(ns tower-struggle.sprites.countdown
  (:require [quil.core :as q]
            [quip.sprite :as qpsprite]
            [quip.tween :as qptween]
            [quip.utils :as qpu]
            [tower-struggle.common :as common]))

(defn seconds
  [remaining]
  (str (int remaining)))

(defn decimals
  [remaining]
  (apply str (rest (format "%.1f" (float (- remaining (int remaining)))))))

(defn draw-countdown-text
  [{:keys [pos remaining font-small font-large]} color]
  (qpsprite/draw-text-sprite
   {:content (seconds remaining)
    :pos     (map + pos [-17 0])
    :offsets [:right]
    :font    font-small
    :color   color})
  (qpsprite/draw-text-sprite
   {:content (decimals remaining)
    :pos     (map + pos [-17 0] [0 0])
    :offsets [:left]
    :font    font-small
    :color   color}))

(defn draw-countdown
  [{:keys [active? remaining color] :as countdown}]
  (let [color (if (and active?
                       (< remaining 5))
                common/orange
                color)]
    (draw-countdown-text (update countdown :pos #(map + % [-2 -2]))
                         common/grey)
    (draw-countdown-text countdown color)))

(defn update-countdown
  [{:keys [active? remaining prev-time] :as countdown}]
  (let [now (System/currentTimeMillis)
        dt  (/ (- now prev-time) 1000)
        new (if active?
              (max 0 (- remaining dt))
              remaining)]
    (-> countdown
        (assoc :remaining new)
        (assoc :prev-time now))))

(defn countdown
  [pos remaining]
  {:sprite-group :countdown
   :uuid         (java.util.UUID/randomUUID)
   :pos          pos
   :color        common/white
   :active?      true
   :remaining    remaining
   :prev-time    (System/currentTimeMillis)
   :font-large   (q/create-font "font/UbuntuMono-Regular.ttf" 120)
   :font-small   (q/create-font "font/UbuntuMono-Regular.ttf" 70)
   :update-fn    update-countdown
   :draw-fn      draw-countdown})

(defn add-time
  [countdown d]
  (-> countdown
      (assoc :active? false)
      (assoc :color common/purple)
      (qptween/add-tween
       (qptween/tween
        :remaining
        3
        :step-count 50
        :on-complete-fn (fn [c]
                          (-> c
                              (assoc :active? true)
                              (assoc :color common/white)))))))
