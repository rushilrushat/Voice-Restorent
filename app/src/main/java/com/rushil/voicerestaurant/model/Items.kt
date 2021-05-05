package com.rushil.voicerestaurant.model

data class Items(var id:String, var name:String, var price:Double)
data class OrderItems(val o_id:String,val i_id:String,val quintity:Int,val totalPrice:Double,val status:String)
