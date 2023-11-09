(ns tower-struggle.scenes.level-01
  (:require [quil.core :as q]
            [quip.sprite :as qpsprite]
            [quip.utils :as qpu]
            [tower-struggle.common :as common]
            [tower-struggle.sprites.tetromino :as t]
            [tower-struggle.sprites.mino :as m]))

(defn sprites
  "The initial list of sprites for this scene"
  []
  [(t/tetromino [300 800])])

(defn draw-level-01
  "Called each frame, draws the current scene to the screen"
  [state]
  (qpu/background common/blue)
  (qpsprite/draw-scene-sprites state))

(defn lock-tetrominos
  "Find any tetrominos which have _just_ about to move where moving down
  would be illegal, lock them down."
  [state]
  (qpsprite/update-sprites-by-pred
   state
   (fn [s]
     (and (= :tetromino (:sprite-group s))
          (= 1 (:fall-delay s)) ; will move next frame
          (let [updated-tetromino (update-in s [:pos 1] + (:h s))]
            (not (t/allowed-location? state updated-tetromino)))))
   (fn [s]
     (assoc s :locked? true))))

(defn add-new-tetrominos
  "Temporary function to keep adding new pieces as they lock in"
  [{:keys [current-scene] :as state}]
  (if (seq (filter :locked? (get-in state [:scenes current-scene :sprites])))
    (update-in state [:scenes current-scene :sprites] conj (t/tetromino [300 100]))
    state))

(defn transfer-locked-tetrominos
  "Move tetrominos which have just been locked in to the longer term
  storage"
  [{:keys [current-scene] :as state}]
  (let [sprites (get-in state [:scenes current-scene :sprites])
        tetrominos (filter (qpsprite/group-pred :tetromino) sprites)
        others (remove (qpsprite/group-pred :tetromino) sprites)
        locked (filter :locked? tetrominos)
        unlocked (remove :locked? tetrominos)]
    (assoc-in state [:scenes current-scene :sprites]
              (concat others
                      unlocked
                      (when (seq locked)
                        (-> locked
                            t/all-minos
                            m/create-multiple))))))

(defn update-level-01
  "Called each frame, update the sprites in the current scene"
  [state]
  (-> state
      qpsprite/update-scene-sprites
      lock-tetrominos
      add-new-tetrominos
      transfer-locked-tetrominos))

(defn handle-left
  [{:keys [current-scene] :as state}]
  (qpsprite/update-sprites-by-pred
   state
   (qpsprite/group-pred :tetromino)
   (partial t/move-left state)))

(defn handle-right
  [{:keys [current-scene] :as state}]
  (qpsprite/update-sprites-by-pred
   state
   (qpsprite/group-pred :tetromino)
   (partial t/move-right state)))

(defn handle-up
  [{:keys [current-scene] :as state}]
  (qpsprite/update-sprites-by-pred
   state
   (qpsprite/group-pred :tetromino)
   (partial t/rotate-clockwise state)))

(defn handle-down
  [{:keys [current-scene] :as state}]
  (qpsprite/update-sprites-by-pred
   state
   (qpsprite/group-pred :tetromino)
   (partial t/rotate-anticlockwise state)))

(defn handle-space-down
  [{:keys [current-scene] :as state}]
  (qpsprite/update-sprites-by-pred
   state
   (qpsprite/group-pred :tetromino)
   t/fast-speed))

(defn handle-space-up
  [{:keys [current-scene] :as state}]
  (qpsprite/update-sprites-by-pred
   state
   (qpsprite/group-pred :tetromino)
   t/default-speed))

;; We need to declare the scene's `init` function ahead of time so we
;; can reference it here.
(declare init)
(defn reset
  [{:keys [current-scene] :as state}]
  (assoc-in state [:scenes current-scene] (init)))

(defn handle-keys
  "Takes the state, the key event that just happened and a map of `{key
  => handler}` where key is either the `:key` field of the event or
  the :key-code field of the event for keys with difficult to
  represent `:key` fields (like enter, esc etc.).

  If the key or key-code from the key event exists in the map, the
  corresponding handler will be called on the state."
  [state e handlers]
  (let [k (:key e)
        kc (:key-code e)]
    (if-let [handler (or (handlers k)
                         (handlers kc))]
      (handler state)
      state)))

(defn handle-key-pressed
  [state e]
  (handle-keys state e
               {:left handle-left
                :right handle-right
                :up handle-up
                :down handle-down
                :space handle-space-down
                :r reset}))

(defn handle-key-released
  [state e]
  (handle-keys state e
               {:space handle-space-up}))

(defn init
  "Initialise this scene"
  []
  {:sprites (sprites)
   :draw-fn draw-level-01
   :update-fn update-level-01
   :key-pressed-fns [handle-key-pressed]
   :key-released-fns [handle-key-released]})
