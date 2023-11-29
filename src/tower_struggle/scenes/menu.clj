(ns tower-struggle.scenes.menu
  (:require [quil.core :as q]
            [quip.sprite :as qpsprite]
            [quip.sprites.button :as qpbutton]
            [quip.scene :as qpscene]
            [quip.sound :as qpsound]
            [quip.utils :as qpu]
            [tower-struggle.common :as common]))

(defn on-click-play
  "Transition from this scene to `:level-01` with a 30 frame fade-out"
  [state e]
  (qpsound/play "paper-flip.wav")
  (qpsound/stop-music)
  (qpscene/transition state :level-01 :transition-length 30))

(defn sprites
  "The initial list of sprites for this scene"
  []
  [(qpsprite/image-sprite
    :background
    [(/ (q/width) 2)
     (- (/ (q/height) 2) 100)]
    1024
    1792
    "img/background.png")
   (qpsprite/text-sprite "TOWER"
                         [(* 0.5 (q/width))
                          (* 0.28 (q/height))]
                         :size (- qpu/title-text-size 30)
                         :color common/yellow)
   (qpsprite/text-sprite "Struggle"
                         [(+ 20 (* 0.5 (q/width)))
                          (+ 70 (* 0.28 (q/height)))]
                         :size qpu/title-text-size
                         :color common/yellow)
   (qpbutton/button-sprite "Play"
                           [(* 0.5 (q/width))
                            (* 0.7 (q/height))]
                           :color common/grey
                           :content-color common/white
                           :on-click on-click-play)])

(defn draw-menu
  "Called each frame, draws the current scene to the screen"
  [{:keys [current-scene] :as state}]
  (qpu/background common/purple)
  (qpsprite/draw-scene-sprites state))

(defn update-menu
  "Called each frame, update the sprites in the current scene"
  [{:keys [current-scene] :as state}]
  (-> state
      qpsprite/update-scene-sprites))

(defn init
  "Initialise this scene"
  []
  (qpsound/loop-music "menu-background-test.wav")
  {:sprites (sprites)
   :draw-fn draw-menu
   :update-fn update-menu
   :mouse-pressed-fns [qpbutton/handle-buttons-pressed]
   :mouse-released-fns [qpbutton/handle-buttons-released]})
