(ns tower-struggle.scenes.level-01
  (:require [quil.core :as q]
            [quip.sprite :as qpsprite]
            [quip.sound :as qpsound]
            [quip.tween :as qptween]
            [quip.utils :as qpu]
            [tower-struggle.common :as common]
            [tower-struggle.sprites.tetromino :as t]
            [tower-struggle.sprites.mino :as m]))

(defn sprites
  "The initial list of sprites for this scene"
  []
  [(qpsprite/image-sprite
    :background
    [(/ (q/width) 2)
     (- (q/height) 2000)]
    1024
    4000
    "img/big-background.png")
   (t/tetromino [300 800])])

(defn tower-outline
  [x y w h]
  (let [tower-width w
        tower-height h
        tl [x y]
        tr [(+ x tower-width) y]
        bl [x (+ y tower-height)]
        br [(+ x tower-width) (+ y tower-height)]]
    {:left   (concat tl bl)
     :right  (concat tr br)
     :top    (concat tl tr)
     :bottom (concat br bl)}))

(defn draw-tower-outline
  "Draws a rectangle of dashed lines using Quil line primitives."
  [x y w h]
  (q/stroke common/black)
  (let [o (tower-outline x y w h)
        dash-length 10]
    (doseq [[key value] o]
      (let [distance (apply q/dist value)
            dash-count (int (/ distance dash-length))
            x1 (first value)
            y1 (second value)
            x2 (nth value 2)
            y2 (nth value 3)
            x-inc (/ (- x2 x1) dash-count)
            y-inc (/ (- y2 y1) dash-count)]
        (doseq [i (range 0 dash-count 2)]
          (let [x-start (+ x1 (* i x-inc))
                y-start (+ y1 (* i y-inc))
                x-end (+ x1 (* (inc i) x-inc))
                y-end (+ y1 (* (inc i) y-inc))]
            (q/line x-start y-start x-end y-end)))))))

(defn draw-level-01
  "Called each frame, draws the current scene to the screen"
  [state]
  
  (qpu/background common/space-black)
  (qpsprite/draw-scene-sprites state)
  (draw-tower-outline 50 50 100 400)

  (when (:debug-mode? state)

    ;; draw framerate
    (q/text-align :left :center)
    (q/text (str "FPS: " (format "%.2f" (:current-framerate state))) 50 50)

    ;; draw processed mino count
    (q/text (str "hot: "
                 (count (filter (qpsprite/group-pred :mino)
                                (get-in state [:scenes (:current-scene state) :sprites]))))
            50 100)

    ; cold storage mino count
    (q/text (str "cold: "
                 (count (get-in state [:scenes (:current-scene state) :off-screen-minos])))
            50 150)))

(defn lock-tetrominos
  "Find any tetrominos which have _just_ about to move where moving down
  would be illegal, lock them down."
  [state]
  (qpsprite/update-sprites-by-pred
   state
   (fn [s]
     (and (= :tetromino (:sprite-group s))
          (= 1 (:fall-delay s)) ; will move next frame
          (let [updated-tetromino (update-in s [:pos 1] + (:h s))]
            (not (t/allowed-location? state updated-tetromino)))))
   (fn [s]
     (qpsound/play "click.wav")
     (assoc s :locked? true))))

(defn add-new-tetrominos
  "Temporary function to keep adding new pieces as they lock in"
  [{:keys [current-scene] :as state}]
  (if (seq (filter :locked? (get-in state [:scenes current-scene :sprites])))
    (update-in state [:scenes current-scene :sprites] conj (t/tetromino [300 100]))
    state))

(defn transfer-locked-tetrominos
  "Move tetrominos which have just been locked in to the longer term
  storage"
  [{:keys [current-scene] :as state}]
  (let [sprites (get-in state [:scenes current-scene :sprites])
        tetrominos (filter (qpsprite/group-pred :tetromino) sprites)
        others (remove (qpsprite/group-pred :tetromino) sprites)
        locked (filter :locked? tetrominos)
        unlocked (remove :locked? tetrominos)]
    (assoc-in state [:scenes current-scene :sprites]
              (concat others
                      unlocked
                      (when (seq locked)
                        (-> locked
                            t/all-minos
                            m/create-multiple))))))

(defn transfer-off-screen-minos
  "Move minos which have moved off the bottom of the screen out of the
  sprites list and into cold storage so we don't process them every
  frame.

  We can retrieve them later for the game end."
  [{:keys [current-scene] :as state}]
  (let [sprites (get-in state [:scenes current-scene :sprites])
        minos (filter (qpsprite/group-pred :mino) sprites)
        others (remove (qpsprite/group-pred :mino) sprites)
        off-screen (filter (fn [m]
                             (<= (q/height) (get-in m [:pos 1])))
                           minos)
        on-screen  (remove (fn [m]
                             (<= (q/height) (get-in m [:pos 1])))
                           minos)]
    (-> state
        (assoc-in [:scenes current-scene :sprites]
                  (concat others
                          on-screen))
        (update-in [:scenes current-scene :off-screen-minos]
                   concat off-screen))))

(defn move-camera
  "Whenever we lock-in tetrominos that end up with a mino above half the
  height of the screen, we should move everything down, effectively
  adjusting the camera position.

  We start to drop below 60fps after updating+drawing ~250 minos, we
  drop to 30fps after ~500.

  This kinda works as even with a near perect fill you can only get
  ~200 minos on screen before we start to scroll."
  [{:keys [current-scene] :as state}]
  ;; since this will only move by one mino-height per frame it takes a
  ;; few frames if we add a tall piece, it's reasonably smooth.
  (let [sprites (get-in state [:scenes current-scene :sprites])
        minos (filter (qpsprite/group-pred :mino) sprites)
        highest (first (sort-by (comp second :pos) minos))]
    (if (and (seq minos)
             (< (get-in highest [:pos 1]) (/ (q/height) 2)))
      (-> state
          ;; move minos down by a mino height
          (qpsprite/update-sprites-by-pred
           #(#{:tetromino :mino} (:sprite-group %))
           (fn [s]
             ;; sorry this is a bit ugly, minos need their
             ;; `display-pos` updated as well so their lock-in
             ;; animations stay in the right place.
             (if (= :mino (:sprite-group s))
               (-> s
                   (update-in [:pos 1] + (:h s))
                   (update-in [:display-pos 1] + (:h s)))
               (-> s
                   (update-in [:pos 1] + (:h s))))))
          ;; move background down by a pixel to give some parallax
          (qpsprite/update-sprites-by-pred
           (qpsprite/group-pred :background)
           (fn [s]
             (update s :pos (partial map +) [0 3]))))
      state)))

(defn update-framerate
  [{:keys [last-frame-time] :as state}]
  (let [now (q/millis)]
    (-> state
        (assoc :current-framerate (float (* 1000 (/ 1 (- now last-frame-time)))))
        (assoc :last-frame-time now))))

(defn update-level-01
  "Called each frame, update the sprites in the current scene"
  [state]
  (-> state
      update-framerate
      qpsprite/update-scene-sprites
      qptween/update-sprite-tweens
      lock-tetrominos
      add-new-tetrominos
      transfer-locked-tetrominos
      transfer-off-screen-minos
      move-camera))

(defn handle-left
  [{:keys [current-scene] :as state}]
  (qpsprite/update-sprites-by-pred
   state
   (qpsprite/group-pred :tetromino)
   (partial t/move-left state)))

(defn handle-right
  [{:keys [current-scene] :as state}]
  (qpsprite/update-sprites-by-pred
   state
   (qpsprite/group-pred :tetromino)
   (partial t/move-right state)))

(defn handle-up
  [{:keys [current-scene] :as state}]
  (qpsprite/update-sprites-by-pred
   state
   (qpsprite/group-pred :tetromino)
   (partial t/rotate-clockwise state)))

(defn handle-down
  [{:keys [current-scene] :as state}]
  (qpsprite/update-sprites-by-pred
   state
   (qpsprite/group-pred :tetromino)
   (partial t/rotate-anticlockwise state)))

(defn handle-space-down
  [{:keys [current-scene] :as state}]
  (qpsprite/update-sprites-by-pred
   state
   (qpsprite/group-pred :tetromino)
   t/fast-speed))

(defn handle-space-up
  [{:keys [current-scene] :as state}]
  (qpsprite/update-sprites-by-pred
   state
   (qpsprite/group-pred :tetromino)
   t/default-speed))

;; We need to declare the scene's `init` function ahead of time so we
;; can reference it here.
(declare init)
(defn reset
  [{:keys [current-scene] :as state}]
  (assoc-in state [:scenes current-scene] (init)))

(defn toggle-debug-mode
  [state]
  (update state :debug-mode? not))

(defn handle-keys
  "Takes the state, the key event that just happened and a map of `{key
  => handler}` where key is either the `:key` field of the event or
  the :key-code field of the event for keys with difficult to
  represent `:key` fields (like enter, esc etc.).

  If the key or key-code from the key event exists in the map, the
  corresponding handler will be called on the state."
  [state e handlers]
  (let [k (:key e)
        kc (:key-code e)]
    (if-let [handler (or (handlers k)
                         (handlers kc))]
      (handler state)
      state)))

(defn handle-key-pressed
  [state e]
  (handle-keys state e
               {:left handle-left
                :right handle-right
                :up handle-up
                :down handle-down
                :space handle-space-down
                :r reset
                :d toggle-debug-mode}))

(defn handle-key-released
  [state e]
  (handle-keys state e
               {:space handle-space-up}))

(defn init
  "Initialise this scene"
  []
  {:sprites (sprites)
   :draw-fn draw-level-01
   :update-fn update-level-01
   :key-pressed-fns [handle-key-pressed]
   :key-released-fns [handle-key-released]})
