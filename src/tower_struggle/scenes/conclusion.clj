(ns tower-struggle.scenes.conclusion
  (:require [quil.core :as q]
            [quip.delay :as qpdelay]
            [quip.scene :as qpscene]
            [quip.sprite :as qpsprite]
            [quip.utils :as qpu]
            [tower-struggle.common :as common]))

(defn set-sprites
  [new-sprites]
  (fn [{:keys [current-scene] :as state}]
    (assoc-in state [:scenes current-scene :sprites]
              new-sprites)))

(defn middle
  []
  [(/ (q/width) 2)
   (/ (q/height) 2)])

(defn texts
  [contents]
  (set-sprites (map #(qpsprite/text-sprite % (middle) :color common/white)
                    contents)))

(defn remove-sprites
  [{:keys [current-scene] :as state}]
  (assoc-in state [:scenes current-scene :sprites] []))

(defn conclusion-delays
  []
  (qpdelay/sequential-delays
   [[100 (texts ["YOUR TOWER IS COMPLETE"])]
    [200 remove-sprites]
    [100 (texts ["THE SKYLINE FOREVER CHANGED"])]
    [200 remove-sprites]
    [100 (texts ["YOU WILL BE REMEMBERED"])]
    [200 remove-sprites]
    [100 (texts ["THIS MUST BE GOOD"])]
    [300 (fn [state]
           (-> state
               ;; AHHH THIS DOESN'T WORK
               ;; (merge (tower-struggle.core/setup))
               ;; (assoc :scenes (tower-struggle.core/init-scenes))
               (qpscene/transition :menu)))]]))

(defn update-conclusion
  [state]
  (-> state
      qpsprite/update-scene-sprites
      qpdelay/update-delays))

(defn draw-conclusion
  [state]
  (qpu/background common/grey)
  (qpsprite/draw-scene-sprites state))

(defn init
  []
  {:sprites []
   :update-fn update-conclusion
   :draw-fn draw-conclusion
   :delays (conclusion-delays)})
