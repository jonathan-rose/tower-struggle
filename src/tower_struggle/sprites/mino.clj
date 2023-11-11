(ns tower-struggle.sprites.mino
  "Once the tetrominos have been locked in, we transform them into
  individual minos so we can more easily detect collisions and
  calculate adjacency effects."
  (:require [quil.core :as q]
            [quip.utils :as qpu]
            [quip.tween :as qptween]
            [tower-struggle.common :as common]))

(defn draw-mino
  [{:keys [display-pos display-w display-h room] :as mino}]
  (let [[x y] display-pos]
    (case room
      :resi (qpu/fill (qpu/darken common/purple))
      :comm (qpu/fill (qpu/darken common/yellow))
      :util (qpu/fill (qpu/darken common/orange)))
    (q/no-stroke)
    (q/rect x y display-w display-h)))

(defn initial-tweens
  "These four tweens shrink a mino's width and height while altering
  it's position to keep it centred.

  We're modifying the display verion of these properties so collision
  detection etc. still works as normal while tweening."
  [i]
  (let [size-delta 10
        pos-delta (/ size-delta 2)
        step-count 2
        i (inc i)]
    [(qptween/tween
      :display-w
      (- size-delta)
      :step-count (* step-count i)
      :yoyo? true)
     (qptween/tween
      :display-h
      (- size-delta)
      :step-count (* step-count i)
      :yoyo? true)
     (qptween/tween
      :display-pos
      pos-delta
      :step-count (* step-count i)
      :update-fn qptween/tween-x-fn
      :yoyo? true
      :yoyo-update-fn qptween/tween-x-yoyo-fn)
     (qptween/tween
      :display-pos
      pos-delta
      :step-count (* step-count i)
      :update-fn qptween/tween-y-fn
      :yoyo? true
      :yoyo-update-fn qptween/tween-y-yoyo-fn)]))

(defn mino
  [pos room size i]
  (let [m (common/simple-sprite
           :mino
           pos
           :w size
           :h size
           :draw-fn draw-mino
           :update-fn identity
           :other {:room room
                   ;; these are for drawing, the real ones are for
                   ;; collisions etc.
                   :display-w size
                   :display-h size
                   :display-pos pos})]
    (reduce qptween/add-tween
            m
            (initial-tweens i))))

(defn create-multiple
  "Create multiple mions at once (presumably from a tetromino which is
  being locked in).

  We want to pass the index into the creation so the initial tweens
  are offset for each mino giving a nice ripple effect."
  [param-lists]
  (map-indexed (fn [i [pos room size]]
                 (mino pos room size i))
               param-lists))
