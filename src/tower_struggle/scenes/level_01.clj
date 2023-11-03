(ns tower-struggle.scenes.level-01
  (:require [quip.sprite :as qpsprite]
            [quip.utils :as qpu]
            [tower-struggle.common :as common]))

(defn sprites
  "The initial list of sprites for this scene"
  []
  [])

(defn draw-level-01
  "Called each frame, draws the current scene to the screen"
  [state]
  (qpu/background common/blue)
  (qpsprite/draw-scene-sprites state))

(defn update-level-01
  "Called each frame, update the sprites in the current scene"
  [state]
  (-> state
      qpsprite/update-scene-sprites))

(defn init
  "Initialise this scene"
  []
  {:sprites (sprites)
   :draw-fn draw-level-01
   :update-fn update-level-01})
