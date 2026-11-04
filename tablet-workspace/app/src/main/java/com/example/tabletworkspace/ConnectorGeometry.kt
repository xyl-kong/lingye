package com.example.tabletworkspace
import kotlin.math.*
fun connectorPoint(c: Connector,t: Float): Point {
 val u=t.coerceIn(0f,1f)
 if(c.pathStyle=="straight") return Point(c.x1+(c.x2-c.x1)*u,c.y1+(c.y2-c.y1)*u)
 if(c.pathStyle=="step") {
  val midX=(c.x1+c.x2)/2; val midY=(c.y1+c.y2)/2
  val startHorizontal=c.startPort in setOf("left","right")
  val endHorizontal=c.endPort in setOf("left","right")
  val first=if(startHorizontal) Point(midX,c.y1) else Point(c.x1,midY)
  val second=if(endHorizontal) Point(midX,c.y2) else Point(c.x2,midY)
  val route=listOf(Point(c.x1,c.y1),first,second,Point(c.x2,c.y2))
  val lengths=route.zipWithNext { a,b -> hypot(b.x-a.x,b.y-a.y) }; val total=lengths.sum(); var remaining=total*u
  for(i in lengths.indices) { if(lengths[i]<=0f) continue; if(remaining<=lengths[i]) { val f=remaining/lengths[i]; return Point(route[i].x+(route[i+1].x-route[i].x)*f,route[i].y+(route[i+1].y-route[i].y)*f) }; remaining-=lengths[i] }
  return route.last()
 }
 val d=max(32f,hypot(c.x2-c.x1,c.y2-c.y1)*0.4f).coerceAtMost(400f)
 fun direction(port:String, fallback:Float): Point = when(port) { "left" -> Point(-1f,0f); "right" -> Point(1f,0f); "top" -> Point(0f,-1f); "bottom" -> Point(0f,1f); else -> Point(fallback,0f) }
 val sign=if(c.x2>=c.x1) 1f else -1f; val start=direction(c.startPort,sign); val end=direction(c.endPort,-sign)
 val x1=c.x1+start.x*d; val y1=c.y1+start.y*d; val x2=c.x2+end.x*d; val y2=c.y2+end.y*d; val v=1-u
 return Point(v*v*v*c.x1+3*v*v*u*x1+3*v*u*u*x2+u*u*u*c.x2, v*v*v*c.y1+3*v*v*u*y1+3*v*u*u*y2+u*u*u*c.y2)
}
fun connectorSamples(c: Connector): List<Point> = (0..40).map { connectorPoint(c,it/40f) }
fun connectorProgress(c: Connector, t: Float): Point {
 val pts=connectorSamples(c); val lengths=pts.zipWithNext { a,b -> hypot(b.x-a.x,b.y-a.y) }; val total=lengths.sum(); var remaining=total*t.coerceIn(0f,1f)
 for(i in lengths.indices) { if(remaining<=lengths[i] && lengths[i]>0f) { val f=remaining/lengths[i]; return Point(pts[i].x+(pts[i+1].x-pts[i].x)*f,pts[i].y+(pts[i+1].y-pts[i].y)*f) }; remaining-=lengths[i] }; return pts.last()
}
fun Page.reverseConnector(index: Int) {
 require(index in connectors.indices)
 val c=connectors[index]
 connectors[index]=c.copy(
  x1=c.x2,y1=c.y2,x2=c.x1,y2=c.y1,
  startShape=c.endShape,endShape=c.startShape,
  startPort=c.endPort,endPort=c.startPort,
  startMarker=c.endMarker,endMarker=c.startMarker
 )
}
