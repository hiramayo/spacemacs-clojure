(ns spacemacs-clojure.core
  (:import (java.awt Color Dimension) 
           (javax.imageio ImageIO)
           (java.io File)
     (javax.swing JPanel JFrame Timer JOptionPane)
     (java.awt.event ActionListener KeyListener))
  (:use examples.import-static))
(import-static java.awt.event.KeyEvent VK_LEFT VK_RIGHT VK_UP VK_DOWN)




; Inspired by the snakes that have gone before:
; Abhishek Reddy's snake: http://www.plt1.com/1070/even-smaller-snake
; Mark Volkmann's snake: http://www.ociweb.com/mark/programming/ClojureSnake.html 

; The START:/END: pairs are production artifacts for the book and not 
; part of normal Clojure style

; ----------------------------------------------------------
; functional mode:l
; ----------------------------------------------------------
; START: constants
(def width 50)
(def height 30)
(def point-size 20)
(def turn-millis 75)
(def win-length 5)
(def dirs { VK_LEFT  [-1  0]
            VK_RIGHT [ 1  0]
            VK_UP    [ 0 -1]
           VK_DOWN  [ 0  1]})
; END: constants
(def item-paths
  {:apple "goal.jpg"
   :snake "kggt.jpg"
   :start "text_start.png"
   :hamburg "hamburg.png"
   :cake "cake.jpg"
   :orange-juice "juice.png"
   :ramen "ramen.png"
   :cheeze-cake "cheeze-cake.gif"})


(def item-imgs
  (zipmap (keys item-paths)(map #(ImageIO/read (File. %)) (vals item-paths)) ))


(def start-point
  [(rand-int width) (rand-int height)])

; START: board math
(defn add-points [& pts] 
  (vec (apply map + pts)))

(defn point-to-screen-rect [pt] 
  (map #(* point-size %) 
       [(pt 0) (pt 1) 1 1]))
; END: board math

; START: apple
(defn make-creater [type]
  (fn [pt]
    {:location pt
     :type type
     :img (item-imgs type)}))

(def create-apple
  (make-creater :apple))
 

; START: snake
(defn create-snake []
  {:location start-point 
   :dir [1 0]
   :type :snake
   :img (item-imgs :snake)
   :color (Color. 15 160 70)})

(def create-start
  (make-creater :start))


(def create-hamburg 
  (make-creater :hamburg))

(def create-cake
  (make-creater :cake))

(def create-cheeze-cake
  (make-creater :cheeze-cake))

(def create-orange-juice
  (make-creater :orange-juice))

(def create-ramen
  (make-creater :ramen))

(defn move [{:keys [location dir] :as snake} & grow]
  (assoc snake :location (add-points location dir)))

; START: turn
(defn turn [snake newdir]
  (assoc snake :dir newdir))
; END: turn



; START: lose?
(defn head-overlaps-location? [{[head & location] :location}]
  (contains? (set location) head))



; START: eats?
(defn eats? [{snake-head :location} {apple :location}]
   (= snake-head apple))
                                        ; END: eats?
(defn eats-hamburg? [{snake-head :location} {hamburg :location}]
  (= snake-head hamburg))

(defn eats-cake? [{snake-head :location} {cake :location}]
  (= snake-head cake))

(defn eats-cheeze-cake? [{snake-head :location} {cheeze-cake :location}]
  (= snake-head cheeze-cake))

(defn eats-orange-juice? [{snake-head :location} {orange-juice :location}]
  (= snake-head orange-juice))

(defn eats-ramen? [{snake-head :location} {ramen :location}]
  (= snake-head ramen))


; START: update-direction
(defn update-direction [snake newdir]
  (when newdir (dosync 
                 (do (alter snake turn newdir)
                   (alter snake move)))))
; END: update-direction

; START: reset-
(defn reset-game [snake apple hamburg]
  (dosync 
   (ref-set hamburg (create-hamburg [(rand-int width) (rand-int height)]))
    )
  nil)

(defn reset-cake [snake apple cake]
  (dosync 
   (ref-set cake (create-cake [(rand-int width) (rand-int height)]))
   )
  nil)

(defn reset-cheeze-cake [snake apple cheeze-cake]
  (dosync 
   (ref-set cheeze-cake (create-cheeze-cake [(rand-int width) (rand-int height)]))
   )
  nil)
(defn reset-orange-juice [snake apple orange-juice]
  (dosync 
   (ref-set orange-juice (create-orange-juice [(rand-int width) (rand-int height)]))
   )
  nil)
(defn reset-ramen [snake apple ramen]
  (dosync 
   (ref-set ramen (create-ramen [(rand-int width) (rand-int height)]))
   )
  nil)

; END: reset-game

; ----------------------------------------------------------
; gui:w
; ----------------------------------------------------------
; START: fill-point
(defn fill-point [g pt color]
  (let [[x y width height] (point-to-screen-rect pt)]
    (.setColor g color)
    (.fillRect g x y width height)))
; END: fill-point

(defn fill-point-by-img [g pt img this]
  (let [[x y width height] (point-to-screen-rect pt)]
    (.drawImage g img x y width height this)))


; START: paint
(defmulti paint (fn [g object & _] (:type object)))


(defmethod paint :default [g {:keys [location img]} this] ; <label id="code.paint.apple"/>
  (fill-point-by-img g location img this))


; END: paint

; START: game-panel
(defn game-panel [frame snake apple start hamburg cake cheeze-cake orange-juice ramen]
  (proxy [JPanel ActionListener KeyListener] []
    (paintComponent [g] ; <label id="code.game-panel.paintComponent"/>
      (proxy-super paintComponent g)
      (paint g @snake this)
      (paint g @apple this)
      (paint g @start this)
      (paint g @cheeze-cake this)
      (paint g @hamburg this)
      (paint g @cake this)
      (paint g @ramen this)
      (paint g @orange-juice this)
      (comment(paint g @frog this)))
    (keyPressed [e] ; <label id="code.game-panel.keyPressed"/>
      (update-direction snake (dirs (.getKeyCode e)))
      (if (eats-hamburg? @snake @hamburg)
        (do (JOptionPane/showMessageDialog frame "ハンバーグを食べた")
             (reset-game snake apple hamburg)))
      (if (eats-cake? @snake @cake)
        (do (JOptionPane/showMessageDialog frame "ケーキを食べた")
             (reset-cake snake apple cake)))
      (if (eats-orange-juice? @snake @orange-juice)
        (do (JOptionPane/showMessageDialog frame "オレンジジュースをのんだ")
             (reset-orange-juice snake apple orange-juice)))
      (when (eats-cheeze-cake? @snake @cheeze-cake)
        (reset-cheeze-cake snake apple cheeze-cake)
             (JOptionPane/showMessageDialog frame "チーズケーキを食べた")
             )
      (when (eats-ramen? @snake @ramen)
        (reset-ramen snake apple ramen)
        (JOptionPane/showMessageDialog frame "ラーメンを食べた")
        )
      (if (eats? @snake @apple)
        (JOptionPane/showMessageDialog frame "ゴール"))
      (.repaint this))
    (actionPerformed [e])
    (getPreferredSize [] 
      (Dimension. (* (inc width) point-size) 
       (* (inc height) point-size)))
    (keyReleased [e])
    (keyTyped [e])))
; END: game-panel

; START: game
(defn game [] 
  (let [start (ref (create-start2))
        snake (ref (create-snake))
        apple (ref (create-apple [20 20]))
        hamburg (ref (create-hamburg [1 1]))
        orange-juice (ref (create-orange-juice [(rand-int width)(rand-int height)]))
        cake (ref (create-cake [(rand-int width)(rand-int height)]))
        cheeze-cake (ref (create-cheeze-cake [(rand-int width)(rand-int height)]))
        ramen (ref (create-ramen [(rand-int width)(rand-int height)]))
        frame (JFrame. "Snake")
        panel (game-panel frame snake apple start hamburg cake cheeze-cake orange-juice ramen)
        timer (Timer. turn-millis panel)]
    (doto panel ; <label id="code.game.panel"/>
      (.setFocusable true)
      (.addKeyListener panel))
    (doto frame ; <label id="code.game.frame"/>
      (.add panel)
      (.pack)
      (.setVisible true))
    [snake, apple, timer])) ; <label id="code.game.return"/>
; END: game


 
  
  

  

