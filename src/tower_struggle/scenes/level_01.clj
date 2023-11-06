(ns tower-struggle.scenes.level-01
  (:require [quip.sprite :as qpsprite]
            [quip.utils :as qpu]
            [tower-struggle.common :as common]
            [tower-struggle.sprites.tetromino :as t]))

(defn sprites
  "The initial list of sprites for this scene"
  []
  [(t/tetromino [300 100])])

(defn draw-level-01
  "Called each frame, draws the current scene to the screen"
  [state]
  (qpu/background common/blue)
  (qpsprite/draw-scene-sprites state))

(defn transfer-locked-tetrominos
  "Move tetrominos which have just been locked in to the longer term
  storage"
  [state]
  ;; @TODO: implement me
  state)

(defn update-level-01
  "Called each frame, update the sprites in the current scene"
  [state]
  (-> state
      qpsprite/update-scene-sprites
      transfer-locked-tetrominos))

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

(defn handle-key-pressed
  [state e]
  (let [handlers {:left handle-left
                  :right handle-right
                  :space handle-space}
        k (:key e)]
    (if-let [handler (handlers k)]
      (handler state)
      state)))

(defn init
  "Initialise this scene"
  []
  {:sprites (sprites)
   :draw-fn draw-level-01
   :update-fn update-level-01
   :key-pressed-fns [handle-key-pressed]})
