(ns tower-struggle.scenes.outro
  (:require [quil.core :as q]
            [quip.utils :as qpu]
            [quip.delay :as qpdelay]
            [quip.scene :as qpscene]
            [quip.sprite :as qpsprite]
            [quip.tween :as qptween]
            [tower-struggle.common :as common]
            [tower-struggle.sprites.mino :as m]))

;; @TODO: so we've got the old minos being moved back into the sprite
;; list, we've got a white background that fades in and all the minos
;; fade to black.
;;
;; Currently we're tweening all the minos (which is a big performance
;; hit) but it lets us smoothly accelerate and decelerate using an
;; easing curve which is nice. It also lets us guarantee that they'll
;; end up in the right position.

(defn cold->hot
  [total-off-screen-rows {:keys [off-screen-rows h] :as m}]
  (-> m
      (assoc :sprite-group :old-mino)
      (update-in [:display-pos 1] + (* h (- total-off-screen-rows off-screen-rows)))))

(defn populate-sprites
  "Need to convert cold storage minos into sprites.

  Also need to create new simple-"
  [{:keys [current-scene off-screen-rows] :as state}]
  (let [off-screen-minos (get-in state [:scenes current-scene :off-screen-minos])]
    (update-in state [:scenes current-scene :sprites]
               concat
               [(common/simple-sprite
                 :background-cover
                 [0 0]
                 :w (q/width)
                 :h (q/height)
                 :draw-fn (fn [{[x y] :pos :keys [w h color]}]
                            (qpu/fill color)
                            (q/rect x y w h))
                 :other {:color (conj common/white 0)})]
               (map (partial cold->hot off-screen-rows) off-screen-minos))))

(def fade-time 50)

(defn add-fade-tweens
  "Add tweens to fade minos to black and background to white"
  [{:keys [current-scene off-screen-rows] :as state}]
  (-> state
      ;; fade minos to black
      (qpsprite/update-sprites-by-pred
       (fn [{sg :sprite-group}] (#{:mino :old-mino} sg))
       (fn [{[red green blue] :color :as s}]
         (reduce qptween/add-tween
                 s
                 [(qptween/tween
                   :color
                   (- red)
                   :step-count fade-time
                   :update-fn (fn [c d] (update c 0 + d)))
                  (qptween/tween
                   :color
                   (- green)
                   :step-count fade-time
                   :update-fn (fn [c d] (update c 1 + d)))
                  (qptween/tween
                   :color
                   (- blue)
                   :step-count fade-time
                   :update-fn (fn [c d] (update c 2 + d)))])))

      ;; fade background-cover to opaque
      (qpsprite/update-sprites-by-pred
       (qpsprite/group-pred :background-cover)
       (fn [cover]
         (qptween/add-tween
          cover
          (qptween/tween
           :color
           255
           :step-count (+ 150 fade-time)
           :update-fn (fn [c d] (update c 3 #(double (+ % d))))))))))

(defn add-movement-tweens
  "Add tweens to move minos up and background back to original pos"
  [{:keys [current-scene off-screen-rows] :as state}]
  (-> state
      ;; move everything up
      (qpsprite/update-sprites-by-pred
       (fn [{sg :sprite-group}] (#{:mino :old-mino} sg))
       (fn [s]
         (qptween/add-tween
          s
          (qptween/tween
           :display-pos
           (- (* 50 off-screen-rows))
           :easing-fn qptween/ease-in-out-quad
           :update-fn qptween/tween-y-fn))))
      ;; move background down
      (qpsprite/update-sprites-by-pred
       (qpsprite/group-pred :background)
       (fn [{:keys [init-pos pos] :as s}]
         (qptween/add-tween
          s
          (qptween/tween
           :pos
           (- (second init-pos) (second pos))
           :easing-fn qptween/ease-in-out-quad
           :update-fn qptween/tween-y-fn))))))

(defn remove-upscreen-minos
  "Delete any minos that go off the top of the screen"
  [{:keys [current-scene] :as state}]
  (update-in state [:scenes current-scene :sprites]
             #(remove (fn [{:keys [sprite-group pos] :as s}]
                        (and (#{:mino :old-mino} sprite-group)
                             (< (second pos) 0)))
                      %)))

(defn update-outro
  [{:keys [outro-started?] :as state}]
  (if-not outro-started?
    (-> state
        populate-sprites
        add-fade-tweens
        (assoc :outro-started? true))    
    (-> state
        qpsprite/update-scene-sprites
        qptween/update-sprite-tweens
        qpdelay/update-delays
        remove-upscreen-minos)))

(defn draw-outro
  [state]
  (qpu/background common/space-black)
  (qpsprite/draw-scene-sprites-by-layers state [:background
                                                :tetromino
                                                :background-cover
                                                :mino
                                                :old-mino]))

(defn init
  []
  {:sprites [] ;; will get populated by the scene transition
   :draw-fn draw-outro
   :update-fn update-outro
   :delays [(qpdelay/delay fade-time add-movement-tweens)
            (qpdelay/delay (+ 200 fade-time)
                           (fn [state]
                             (qpscene/transition state :conclusion)))]})
