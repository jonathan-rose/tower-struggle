(ns tower-struggle.sprites.mino
  "Once the tetrominos have been locked in, we transform them into
  individual minos so we can more easily detect collisions and
  calculate adjacency effects."
  (:require [quil.core :as q]
            [quip.utils :as qpu]
            [tower-struggle.common :as common]))

(defn draw-mino
  [{:keys [pos w h room] :as mino}]
  (let [[x y] pos]
    (case room
      :resi (qpu/fill (qpu/darken common/purple))
      :comm (qpu/fill (qpu/darken common/yellow))
      :util (qpu/fill (qpu/darken common/orange)))
    (q/no-stroke)
    (q/rect x y w h)))

(defn mino
  [pos room size]
  (common/simple-sprite
   :mino
   pos
   :w size
   :h size
   :draw-fn draw-mino
   :update-fn identity
   :other {:room room}))

(defn create-multiple
  [param-lists]
  (map (partial apply mino)
       param-lists))
