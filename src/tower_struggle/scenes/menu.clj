(ns tower-struggle.scenes.menu
  (:require [quil.core :as q]
            [quip.delay :as qpdelay]
            [quip.sprite :as qpsprite]
            [quip.sprites.button :as qpbutton]
            [quip.scene :as qpscene]
            [quip.sound :as qpsound]
            [quip.utils :as qpu]
            [tower-struggle.common :as common]))

;; just a little animation for now

(defn update-square
  [{:keys [rvel] :as s}]
  (-> s
      (qpsprite/update-pos)
      (update :rotation + rvel)))

(defn draw-square
  [{[x y] :pos
    :keys [color size rotation]
    :as s}]
  (q/no-stroke)
  (qpu/fill color)
  (q/rect-mode :center)
  (qpu/wrap-trans-rot
   [x y]
   rotation
   #(q/rect 0 0 size size))
  (q/rect-mode :corner))

(defn square
  []
  (let [max-size 60]
    {:sprite-group :squares
     :pos [(rand-int (q/width))
           (- (* 2 max-size))]
     :vel [0 (rand-nth (range 2 9))]
     :rvel (- (rand 16) 8)
     :rotation 0
     :color (rand-nth (concat (take 3 (iterate qpu/darken common/orange))
                              (take 3 (iterate qpu/lighten common/orange))))
     :size (rand-nth (range 30 max-size))
     :update-fn update-square
     :draw-fn draw-square}))

(defn on-click-play
  "Transition from this scene to `:level-01` with a 30 frame fade-out"
  [state e]
  (qpsound/stop lobby-sounds)
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

(defn clean-old-squares
  [{:keys [current-scene] :as state}]
  (update-in state [:scenes current-scene :sprites]
             (fn [sprites]
               (remove (fn [{[x y] :pos :keys [sprite-group size]}]
                         (when (= :squares sprite-group)
                           (< (+ (q/height)
                                 (* size 2))
                              y)))
                       sprites))))

(defn update-menu
  "Called each frame, update the sprites in the current scene"
  [{:keys [current-scene] :as state}]
  (-> state
      qpsprite/update-scene-sprites
      qpdelay/update-delays
      clean-old-squares))

(defn add-square-delay
  []
  (qpdelay/delay
    20
    (fn [{:keys [current-scene] :as state}]
      (-> state
          (update-in [:scenes current-scene :sprites] conj (square))
          (qpdelay/add-delay (add-square-delay))))))

(defn init
  "Initialise this scene"
  []
  (def lobby-sounds (qpsound/loop-music "menu-background-test.wav"))
  {:sprites (sprites)
   :draw-fn draw-menu
   :update-fn update-menu
   :mouse-pressed-fns [qpbutton/handle-buttons-pressed]
   :mouse-released-fns [qpbutton/handle-buttons-released]
   :delays [(add-square-delay)]})
