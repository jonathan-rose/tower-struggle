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
            [quip.sound :as qpsound]
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

(def default-max-fall-delay 40)
(def fast-max-fall-delay 2)
(def mino-size 50)

(defn update-tetromino
  [{:keys [w h fall-delay max-fall-delay locked?] :as t}]
  (if locked?
    t
    (if (zero? fall-delay)
      (-> t
          (assoc :fall-delay max-fall-delay)
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
            :fall-delay default-max-fall-delay
            :max-fall-delay default-max-fall-delay
            :locked? false})))

(defn allowed-location?
  [{:keys [current-scene] :as state} {:keys [pos w h current-rotation rotations] :as t}]
  (let [[x y] pos
        sprites (get-in state [:scenes current-scene :sprites])
        locked-minos (filter (qpsprite/group-pred :mino) sprites)
        minos (get rotations current-rotation)]
    (not (some (fn my-minos [[m [dx dy]]]
                 (or
                  ;; left boundary
                  (< (+ x (* w dx)) 0)
                  ;; right boundary
                  (<= (q/width) (+ x (* w dx)))
                  ;; bottom boundary
                  (<= (q/height) (+ y (* h dy)))
                  ;; check other previously locked minos
                  (some (fn their-minos [{locked-pos :pos}]
                          (= [(+ x (* w dx))
                              (+ y (* h dy))]
                             locked-pos))
                        locked-minos)))
               minos))))

(defn play-rotate-sound
  []
  (qpsound/play "pop1.wav"))

(defn move-left
  [state {:keys [locked? pos] :as t}]
  (if locked?
    t
    (let [updated-t (update-in t [:pos 0] - (:w t))]
      (if (allowed-location? state updated-t)
        updated-t
        t))))

(defn move-right
  [state {:keys [locked? pos] :as t}]
  (if locked?
    t
    (let [updated-t (update-in t [:pos 0] + (:w t))]
      (if (allowed-location? state updated-t)
        updated-t
        t))))

(defn rotate-clockwise
  [state {:keys [locked?] :as t}]
  (if locked?
    t
    (do (play-rotate-sound)
        (let [updated-t (update t :current-rotation (fn [n] (mod (inc n) 4)))]
          (if (allowed-location? state updated-t)
            updated-t
            t)))))

(defn rotate-anticlockwise
  [state {:keys [locked?] :as t}]
  (if locked?
    t
    (do (play-rotate-sound)
        (let [updated-t (update t :current-rotation (fn [n] (mod (dec n) 4)))]
         (if (allowed-location? state updated-t)
           updated-t
           t)))))

(defn fast-speed
  [{:keys [max-fall-delay] :as t}]
  (if (= max-fall-delay default-max-fall-delay)
    (-> t
        (assoc :max-fall-delay fast-max-fall-delay)
        (assoc :fall-delay fast-max-fall-delay))
    t))

(defn default-speed
  [{:keys [max-fall-delay] :as t}]
  (if (= max-fall-delay fast-max-fall-delay)
    (-> t
        (assoc :max-fall-delay default-max-fall-delay)
        (assoc :fall-delay default-max-fall-delay))
    t))

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
