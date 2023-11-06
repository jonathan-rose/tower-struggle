(ns tower-struggle.common
  (:require [quil.core :as q]
            [quip.utils :as qpu]
            [quip.sprite :as qpsprite]
            [clojure.string :as s]))

;; @TODO: would be useful to add this to quip
(defn get-screen-size []
  (let [size (.getScreenSize (java.awt.Toolkit/getDefaultToolkit))]
    [(.width size)
     (.height size)]))


;; @TODO: update quip with this version
(defn hex->rgb
  "Updated version of qpu/hex->rgb, returns vectors"
  [hex-string]
  (->> hex-string
       (partition 2)
       (map (partial apply str "0x"))
       (mapv read-string)))


;; @TODO: add coolors.co helper functions to quip somewhere
(defn url->rgbs
  "Convert a coolors.co url into a set of RGB vectors"
  [url]
  (mapv hex->rgb
        (-> url
            (s/split #"/")
            last
            (s/split #"-"))))


(defn url->defs
  "Create a list of defs for a coolors.co url and a list of names"
  [url names]
  (let [rgbs (url->rgbs url)]
    (map (fn [rgb n]
           (list `def (symbol n) rgb))
         rgbs
         names)))

(def orange [237 106 90])
(def yellow [244 241 187])
(def purple [93 87 107])
(def blue [155 193 188])
(def white [230 235 224])
(def grey [57 57 58])
(def black [19 18 0])

(defn draw-simple-sprite
  "Simple box with a cross."
  [{:keys [w h] [x y] :pos}]
  (qpu/stroke qpu/red)
  (qpu/fill qpu/white)
  (q/rect x y w h)
  (q/line [x y] [(+ x w) (+ y h)])
  (q/line [x (+ y h)] [(+ x w) y]))

;; @TODO: this should be backported into quip I've been wanting it for
;; ages. In fact, all the default sprite functions in quip should use
;; this merge other pattern, it's a really common use case.
(defn simple-sprite
  "A basic sprite for when you know you need a custom draw function."
  [sprite-group pos &
   {:keys [w
           h
           update-fn
           draw-fn
           bounds-fn
           other]
    :or {w 50
         h 50
         update-fn identity
         draw-fn draw-simple-sprite
         bounds-fn qpsprite/default-bounding-poly
         other {}}}]
  (merge {:sprite-group sprite-group
          :uuid (random-uuid)
          :pos pos
          :w w
          :h h
          :update-fn update-fn
          :draw-fn draw-fn
          :bounds-fn bounds-fn}
         other))
