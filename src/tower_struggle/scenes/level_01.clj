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
  [(t/tetromino [300 900])])

(defn draw-level-01
  "Called each frame, draws the current scene to the screen"
  [state]
  (qpu/background common/blue)
  (qpsprite/draw-scene-sprites state))

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
  "Find any tetrominos which have _just_ about to move and are sitting
  on top of a locked mino, lock them down."
  [{:keys [current-scene] :as state}]
  (let [sprites (get-in state [:scenes current-scene :sprites])
        locked-minos (filter (qpsprite/group-pred :mino) sprites)]
    (qpsprite/update-sprites-by-pred
     state
     (fn [s]
       (and (= :tetromino (:sprite-group s))
            (= 1 (:fall-delay s))  ; will move next frame
            (let [{:keys [pos w h current-rotation rotations]} s
                  [x y] pos
                  minos (get rotations current-rotation)]
              (some (fn my-minos [[m [dx dy]]]
                      ;; the `(inc dy)` here is because we are interested in
                      ;; the _bottom_ edge of the mino
                      (or (<= (q/height) (+ y (* h (inc dy))))
                          ;; check if other previously locked minos beneath
                          (some (fn their-minos [{locked-pos :pos}]
                                  (= [(+ x (* w dx))
                                      (+ y (* h (inc dy)))]
                                     locked-pos))
                                locked-minos)))
                    minos))))
     (fn [s]
       (assoc s :locked? true)))))

(defn update-level-01
  "Called each frame, update the sprites in the current scene"
  [state]
  (-> state
      qpsprite/update-scene-sprites
      lock-tetrominos
      transfer-locked-tetrominos))

;; @TODO: these left/right functions need to take into account locked
;; minos so we don't move into them.
(defn handle-left
  [{:keys [current-scene] :as state}]
  (qpsprite/update-sprites-by-pred
   state
   (qpsprite/group-pred :tetromino)
   t/move-left))

(defn handle-right
  [{:keys [current-scene] :as state}]
  (qpsprite/update-sprites-by-pred
   state
   (qpsprite/group-pred :tetromino)
   t/move-right))

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
