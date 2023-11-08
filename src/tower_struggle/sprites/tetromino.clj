(ns tower-struggle.sprites.tetromino
  "Standard names for Tetrominos are I, O, T, S, Z, J, L.

  We're describing each rotation of a tetromino by listing the
  relative coordinates of it's minos (squares) as they appear in the
  4x4 grids shown in the comments above each definition.

  The minos are labelled a-d since they can contain different kinds of
  room, this means we need to keep track of where each mino goes
  between rotations.

  @NOTE: the grids are 8x4, but this is to compensate for the 1x2
  monospace character height."
  (:require [quil.core :as q]
            [quip.sprite :as qpsprite]
            [quip.utils :as qpu]
            [tower-struggle.common :as common]))

;; ░░░░░░░░  ░░░░██░░  ░░░░░░░░  ░░██░░░░
;; ████████  ░░░░██░░  ░░░░░░░░  ░░██░░░░
;; ░░░░░░░░  ░░░░██░░  ████████  ░░██░░░░
;; ░░░░░░░░  ░░░░██░░  ░░░░░░░░  ░░██░░░░
(def i-rotations
  [{:a [0 1] :b [1 1] :c [2 1] :d [3 1]}
   {:a [2 0] :b [2 1] :c [2 2] :d [2 3]}
   {:a [3 2] :b [2 2] :c [1 2] :d [0 2]}
   {:a [1 3] :b [1 2] :c [1 1] :d [1 0]}])

;; ░░████░░  ░░████░░  ░░████░░  ░░████░░
;; ░░████░░  ░░████░░  ░░████░░  ░░████░░
;; ░░░░░░░░  ░░░░░░░░  ░░░░░░░░  ░░░░░░░░
;; ░░░░░░░░  ░░░░░░░░  ░░░░░░░░  ░░░░░░░░
(def o-rotations
  [{:a [1 0] :b [2 0] :c [2 1] :d [1 1]}
   {:a [2 0] :b [2 1] :c [1 1] :d [1 0]}
   {:a [2 1] :b [1 1] :c [1 0] :d [2 0]}
   {:a [1 1] :b [1 0] :c [2 0] :d [2 1]}])

;; ░░██░░░░  ░░██░░░░  ░░░░░░░░  ░░██░░░░
;; ██████░░  ░░████░░  ██████░░  ████░░░░
;; ░░░░░░░░  ░░██░░░░  ░░██░░░░  ░░██░░░░
;; ░░░░░░░░  ░░░░░░░░  ░░░░░░░░  ░░░░░░░░
(def t-rotations
  [{:a [1 0] :b [0 1] :c [1 1] :d [2 1]}
   {:a [2 1] :b [1 0] :c [1 1] :d [1 2]}
   {:a [1 2] :b [2 1] :c [1 1] :d [0 1]}
   {:a [0 1] :b [1 2] :c [1 1] :d [1 0]}])

;; ░░████░░  ░░██░░░░  ░░░░░░░░  ██░░░░░░
;; ████░░░░  ░░████░░  ░░████░░  ████░░░░
;; ░░░░░░░░  ░░░░██░░  ████░░░░  ░░██░░░░
;; ░░░░░░░░  ░░░░░░░░  ░░░░░░░░  ░░░░░░░░
(def s-rotations
  [{:a [1 0] :b [2 0] :c [0 1] :d [1 1]}
   {:a [2 1] :b [2 2] :c [1 0] :d [1 1]}
   {:a [1 2] :b [0 2] :c [2 1] :d [1 1]}
   {:a [0 1] :b [0 0] :c [1 2] :d [1 1]}])

;; ████░░░░  ░░░░██░░  ░░░░░░░░  ░░██░░░░
;; ░░████░░  ░░████░░  ████░░░░  ████░░░░
;; ░░░░░░░░  ░░██░░░░  ░░████░░  ██░░░░░░
;; ░░░░░░░░  ░░░░░░░░  ░░░░░░░░  ░░░░░░░░
(def z-rotations
  [{:a [0 0] :b [1 0] :c [1 1] :d [2 1]}
   {:a [2 0] :b [2 1] :c [1 1] :d [1 2]}
   {:a [2 2] :b [1 2] :c [1 1] :d [0 1]}
   {:a [0 2] :b [0 1] :c [1 1] :d [1 0]}])

;; ██░░░░░░  ░░████░░  ░░░░░░░░  ░░██░░░░
;; ██████░░  ░░██░░░░  ██████░░  ░░██░░░░
;; ░░░░░░░░  ░░██░░░░  ░░░░██░░  ████░░░░
;; ░░░░░░░░  ░░░░░░░░  ░░░░░░░░  ░░░░░░░░
(def j-rotations
  [{:a [0 0] :b [0 1] :c [1 1] :d [2 1]}
   {:a [2 0] :b [1 0] :c [1 1] :d [1 2]}
   {:a [2 2] :b [2 1] :c [1 1] :d [0 1]}
   {:a [0 2] :b [1 2] :c [1 1] :d [1 0]}])

;; ░░░░██░░  ░░██░░░░  ░░░░░░░░  ████░░░░
;; ██████░░  ░░██░░░░  ██████░░  ░░██░░░░
;; ░░░░░░░░  ░░████░░  ██░░░░░░  ░░██░░░░
;; ░░░░░░░░  ░░░░░░░░  ░░░░░░░░  ░░░░░░░░
(def l-rotations
  [{:a [2 0] :b [0 1] :c [1 1] :d [2 1]}
   {:a [2 2] :b [1 0] :c [1 1] :d [1 2]}
   {:a [0 2] :b [2 1] :c [1 1] :d [0 1]}
   {:a [0 0] :b [1 2] :c [1 1] :d [1 0]}])

(def piece-rotations
  {:i i-rotations
   :o o-rotations
   :t t-rotations
   :s s-rotations
   :z z-rotations
   :j j-rotations
   :l l-rotations})

(def room-types [:resi :comm :util])

;; @TODO: could bias towards certain rooms types?
(defn random-rooms
  []
  (zipmap [:a :b :c :d]
          (repeatedly #(rand-nth room-types))))

(defn draw-tetromino
  [{:keys [pos w h current-rotation rotations rooms] :as tetromino}]
  (let [[x y] pos
        minos (get rotations current-rotation)]
    (doseq [[m [dx dy]] minos]
      (let [room (get rooms m)]
        (case room
          :resi (qpu/fill common/purple)
          :comm (qpu/fill common/yellow)
          :util (qpu/fill common/orange))
        (q/no-stroke)
        (q/rect (+ x (* w dx))
                (+ y (* h dy))
                w
                h)))))

(def initial-fall-delay 40)
(def mino-size 50)

(defn update-tetromino
  [{:keys [w h fall-delay locked?] :as t}]
  (if locked?
    t
    (if (zero? fall-delay)
      (-> t
          (assoc :fall-delay initial-fall-delay)
          (update-in [:pos 1] + h))
      (-> t
          (update :fall-delay dec)))))

(defn tetromino
  ([pos]
   (tetromino pos (rand-nth (keys piece-rotations))))
  ([pos piece]
   (common/simple-sprite
    :tetromino
    pos
    :w mino-size
    :h mino-size
    :draw-fn draw-tetromino
    :update-fn update-tetromino
    :other {:piece piece
            :rotations (get piece-rotations piece)
            :current-rotation 0
            :rooms (random-rooms)
            :fall-delay initial-fall-delay
            :locked? false})))

(defn move-left
  [{:keys [locked? pos] :as t}]
  (if locked?
    t
    (update-in t [:pos 0] - mino-size)))

(defn move-right
  [{:keys [locked? pos] :as t}]
  (if locked?
    t
    (update-in t [:pos 0] + mino-size)))

(defn all-minos
  [ts]
  (mapcat (fn [{:keys [pos w h current-rotation rotations rooms] :as tetromino}]
            (let [[x y] pos
                  minos (get rotations current-rotation)]
              (for [[m [dx dy]] minos]
                (let [room (get rooms m)]
                  [[(+ x (* w dx))
                    (+ y (* h dy))]
                   room
                   (if (= w h) w mino-size)]))))
          ts))
