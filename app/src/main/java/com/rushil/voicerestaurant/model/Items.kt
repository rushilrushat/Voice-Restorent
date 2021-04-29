package com.rushil.voicerestaurant.model

data class Items(val id:String,val name:String,val price:String)
data class OrderItems(val o_id:String,val i_id:String,val i_name:String,val quintity:String,val totalPrice:String)
