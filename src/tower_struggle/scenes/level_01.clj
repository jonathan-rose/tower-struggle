(ns tower-struggle.scenes.level-01
  (:require [quil.core :as q]
            [quip.sprite :as qpsprite]
            [quip.utils :as qpu]
            [tower-struggle.common :as common]
            [tower-struggle.sprites.tetromino :as t]
            [tower-struggle.sprites.mino :as m]))

(defn sprites
  "The initial list of sprites for this scene"
  []
  [(t/tetromino [300 800])])

(defn tower-outline
  [x y w h]
  (let [tower-width w
        tower-height h
        coords {:tl  [x y]
                :tr  [(+ x tower-width) y]
                :bl  [x (+ y tower-height)]
                :br  [(+ x tower-width) (+ y tower-height)]}
        sides {:left   (concat (coords :tl) (coords :bl))
               :right  (concat (coords :tr) (coords :br))
               :top    (concat (coords :tl) (coords :tr))
               :bottom (concat (coords :br) (coords :bl))}]
    sides))

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
  (qpu/background common/blue)
  (qpsprite/draw-scene-sprites state)
  (draw-tower-outline 50 50 100 400))

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

(defn lock-tetrominos
  "Find any tetrominos which have _just_ about to move where moving down
  would be illegal, lock them down."
  [state]
  (qpsprite/update-sprites-by-pred
   state
   (fn [s]
     (and (= :tetromino (:sprite-group s))
          (= 1 (:fall-delay s))  ; will move next frame
          (let [updated-tetromino (update-in s [:pos 1] + (:h s))]
            (not (t/allowed-location? state updated-tetromino)))))
   (fn [s]
     (assoc s :locked? true))))

(defn update-level-01
  "Called each frame, update the sprites in the current scene"
  [state]
  (-> state
      qpsprite/update-scene-sprites
      lock-tetrominos
      transfer-locked-tetrominos))

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

(defn handle-space
  [{:keys [current-scene] :as state}]
  (prn "space")
  state)

(defn handle-enter
  [{:keys [current-scene] :as state}]
  (update-in state [:scenes current-scene :sprites] conj (t/tetromino [300 100])))

(defn handle-key-pressed
  [state e]
  (let [handlers {:left handle-left
                  :right handle-right
                  :up handle-up
                  :down handle-down
                  :space handle-space
                  10 handle-enter}
        k (:key e)
        kc (:key-code e)]
    (if-let [handler (or (handlers k)
                         (handlers kc))]
      (handler state)
      state)))

(defn init
  "Initialise this scene"
  []
  {:sprites (sprites)
   :draw-fn draw-level-01
   :update-fn update-level-01
   :key-pressed-fns [handle-key-pressed]})
