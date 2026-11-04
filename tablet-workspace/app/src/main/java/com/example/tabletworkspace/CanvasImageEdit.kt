package com.example.tabletworkspace

fun Page.rotateCanvasImage(index:Int) {
 val image=images.getOrNull(index) ?: return
 val centerX=image.x+image.width/2f;val centerY=image.y+image.height/2f
 images[index]=image.copy(x=centerX-image.height/2f,y=centerY-image.width/2f,width=image.height,height=image.width,rotation=(image.rotation+1)%4,flipX=image.flipY,flipY=image.flipX)
}
fun Page.flipCanvasImage(index:Int,horizontal:Boolean) {
 val image=images.getOrNull(index) ?: return
 images[index]=if(horizontal) image.copy(flipX=!image.flipX) else image.copy(flipY=!image.flipY)
}
fun validImageCrop(left:Float,top:Float,right:Float,bottom:Float):Boolean = left.isFinite() && top.isFinite() && right.isFinite() && bottom.isFinite() && left>=0f && top>=0f && right<=1f && bottom<=1f && right-left>=0.05f && bottom-top>=0.05f
fun Page.cropCanvasImage(index:Int,left:Float,top:Float,right:Float,bottom:Float) {
 require(validImageCrop(left,top,right,bottom))
 val image=images.getOrNull(index) ?: return
 val xRatio=(right-left)/(image.cropRight-image.cropLeft);val yRatio=(bottom-top)/(image.cropBottom-image.cropTop)
 val horizontal=if(image.rotation%2==0) xRatio else yRatio;val vertical=if(image.rotation%2==0) yRatio else xRatio
 val width=(image.width*horizontal).coerceIn(12f,2000f);val height=(image.height*vertical).coerceIn(12f,2000f)
 images[index]=image.copy(width=width,height=height,cropLeft=left,cropTop=top,cropRight=right,cropBottom=bottom)
}
