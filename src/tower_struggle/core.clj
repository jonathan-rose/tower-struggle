(ns tower-struggle.core
  (:gen-class)
  (:require [quip.core :as qp]
            [quip.sound :as qpsound]
            [tower-struggle.scenes.menu :as menu]
            [tower-struggle.scenes.level-01 :as level-01]
            [tower-struggle.scenes.outro :as outro]
            [tower-struggle.scenes.conclusion :as conclusion]
            [tower-struggle.common :as common]))

(defn setup
  "The initial state of the game"
  []
  {:current-framerate 0.0
   :last-frame-time 1
   :debug-mode? false
   :outro-started? false
   :off-screen-rows 0
   :score 0})

(defn init-scenes
  "Map of scenes in the game"
  []
  {:menu     (menu/init)
   :level-01 (level-01/init)
   :outro (outro/init)
   :conclusion (conclusion/init)})

;; Configure the game
(def tower-struggle-game
  (qp/game {:title          "tower-struggle"
            :size           [800
                             (min (- (second (common/get-screen-size))
                                     100)
                                  2000)]
            :setup          setup
            :init-scenes-fn init-scenes
            :current-scene  :menu
            :on-close (fn [_]
                        (qpsound/stop-music))}))

(defn -main
  "Run the game"
  [& args]
  (qp/run tower-struggle-game))
