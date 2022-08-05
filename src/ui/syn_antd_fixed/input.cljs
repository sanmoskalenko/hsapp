(ns ui.syn-antd-fixed.input
  (:require
    ["antd/es/input" :default ant-input]
    ["antd/es/input-number" :default ant-input-number]
    [reagent.core]))


;; Фикс проблемы с тем, что не пробрасываются пропсы в при использовании совместно с
;; Form -> FormInput -> Input из-за ошибки в обертке syn-antd.reagent-utils/fixed-async-input
;; На данный момент проблема решена удалением этой обертки.

(def input (reagent.core/adapt-react-class ant-input))

(def input-group (reagent.core/adapt-react-class (.-Group ant-input)))

(def input-password (reagent.core/adapt-react-class (.-Password ant-input)))

(def input-search (reagent.core/adapt-react-class (.-Search ant-input)))

(def input-text-area (reagent.core/adapt-react-class (.-TextArea ant-input)))

(def input-number (reagent.core/adapt-react-class ant-input-number))