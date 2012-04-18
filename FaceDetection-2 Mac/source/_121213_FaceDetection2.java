import processing.core.*; 

import pbox2d.*; 
import org.jbox2d.collision.shapes.*; 
import org.jbox2d.common.*; 
import org.jbox2d.dynamics.*; 
import org.jbox2d.dynamics.joints.*; 
import org.jbox2d.dynamics.contacts.*; 
import hypermedia.video.*; 
import java.awt.Rectangle; 

import java.applet.*; 
import java.awt.Dimension; 
import java.awt.Frame; 
import java.awt.event.MouseEvent; 
import java.awt.event.KeyEvent; 
import java.awt.event.FocusEvent; 
import java.awt.Image; 
import java.io.*; 
import java.net.*; 
import java.text.*; 
import java.util.*; 
import java.util.zip.*; 
import java.util.regex.*; 

public class _121213_FaceDetection2 extends PApplet {










// A reference to our box2d world
PBox2D box2d;
int zoom=3; //Scaling up the display


// A list we'll use to track fixed objects
ArrayList<Boundary> boundaries;
// A list for all of our rectangles
ArrayList<Ball> balls;

// SPRING VARIABLE
Spring spring1;

//PADDLE VARAIABLES 
Paddle paddle1;


//OPEN CV VARIABLES 
OpenCV opencv;
int contrast_value    = 0;
int brightness_value  = 0;

//TIMER VARIABLES
Timer timer;


public void setup() {
  size(640, 480);
  smooth();

  // Initialize box2d physics and create the world
  box2d = new PBox2D(this);
  box2d.createWorld();
  box2d.listenForCollisions();
  // We are setting a custom gravity
  box2d.setGravity(0, -20);

  // Create ArrayLists	
  balls = new ArrayList<Ball>();
  boundaries = new ArrayList<Boundary>();

  // Set Boundaries of the Wall
  //  boundaries.add(new Boundary(width/4, height-5, width/2-50, 10));
  //  boundaries.add(new Boundary(3*width/4, height-5, width/2-50, 10));
  boundaries.add(new Boundary(width/2, height-5, width, 10));
  boundaries.add(new Boundary(width-5, height/2, 10, height));
  boundaries.add(new Boundary(5, height/2, 10, height));

  // Add Paddles for 
  //  paddle1=new Paddle(faces[0].x+faces[0].width/2, faces[0].y+faces[0].height/2);
  paddle1=new Paddle(width/2, height/2);
  // Make springs- They will only actually be made when faces are detected
  spring1 = new Spring();
  spring1.bind(width/2+10, height/2+10, paddle1);


  //OPENCVSETUP
  opencv = new OpenCV( this );
  opencv.capture( width/zoom, height/zoom);                   // START VIDEO
  opencv.cascade( OpenCV.CASCADE_FRONTALFACE_ALT );  // load detection description, here-> front face detection : "haarcascade_frontalface_alt.xml"


  //TIMER
  timer=new Timer(100);
  timer.start();
  
//  // Add a listener to listen for collisions!
//box2d.world.setContactListener(new CustomListener());
}





public void draw() {
  background(255);
  opencv.read();//GRAB A NEW FRAME
  opencv.flip( OpenCV.FLIP_HORIZONTAL ); // FLIP IMAGE HORIZONTALLY
  //  opencv.convert( GRAY );
  opencv.contrast( contrast_value );
  opencv.brightness( brightness_value );

  // proceed detection
  Rectangle[] faces = opencv.detect( 1.2f, 2, OpenCV.HAAR_DO_CANNY_PRUNING, 40, 40 );

  // display the image
  image( opencv.image(), 0, 0,width,height );

  // We must always step through time!
  box2d.step();
  
    for ( int i=0; i<faces.length; i++ ) {
    //SCALE THE SIZE BACK UP
    faces[i].x*=zoom;
    faces[i].y*=zoom;
    faces[i].width*=zoom;
    faces[i].height*=zoom;}

  ////FACE DETECTION PADDLE MOVEMENT
  for ( int i=0; i<constrain(faces.length,0,3); i++ ) {    
    //    println("faces X" + faces[0].x);
    //    println("faces Y" + faces[0].y);
    if (faces.length>0) {
      spring1.update(faces[i].x+faces[i].width/2, faces[0].y+faces[i].height/2);
      noFill();
      stroke(255, 0, 0);
      strokeWeight(2);
      rectMode(CORNER);
      rect(faces[i].x, faces[i].y, faces[i].width, faces[i].height );
    }
    else {
      spring1.destroy();
    }
  }

  // Display all the boundaries
  for (Boundary wall: boundaries) {
    wall.display();
  }

  // Display all the boxes
  for (Ball b: balls) {
    b.display();
  }

  // Display Paddle
  paddle1.display();
//  spring1.display();

  // Ball that leave the screen, we delete them
  // (note they have to be deleted from both the box2d world and our list

  for (int i = balls.size()-1; i >= 0; i--) {
    Ball b = balls.get(i);
    if (b.done()) {
      balls.remove(i);
    }
  }
    if (timer.isFinished()) {
      // Add a new ball
      Ball p = new Ball(random(0,width), 35, random(5, 10));
      balls.add(p); 
      // Start timer again
      timer.start();
   }
  if (mousePressed) {
    Ball p = new Ball(mouseX, mouseY, random(5, 10));
    balls.add(p);
  }
}

class Timer {

  int savedTime; // Started Timer
  int totalTime; // How long Timer should last

  Timer(int tempTotalTime) {
    totalTime = tempTotalTime;
  }

  // Starting the timer
  public void start() {
    // When the timer starts it stores the current time in milliseconds.
    savedTime = millis();
  }

  // The function isFinished() returns true if 5,000 ms have passed. 
  // The work of the timer is farmed out to this method.
  public boolean isFinished() { 
    // Check how much time has passed
    int passedTime = millis()- savedTime;
    if (passedTime > totalTime) {
      return true;
    } 
    else {
      return false;
    }
  }
}

class Ball {

  // We need to keep track of a Body and a radius
  Body body;
  float r;
  int col;

  Ball(float x, float y, float r_) {
    r = r_;
    // This function puts the particle in the Box2d world
    makeBody(x, y, r);
  }

  // This function removes the particle from the box2d world
  public void killBody() {
    box2d.destroyBody(body);
  }

  // Is the particle ready for deletion?
  public boolean done() {
    // Let's find the screen position of the particle
    Vec2 pos = box2d.getBodyPixelCoord(body);
    // Is it off the bottom of the screen?
    if (pos.y > height+r*2) {
      killBody();
      return true;
    }
    return false;
  }

  // 
  public void display() {
    // We look at each body and get its screen position
    Vec2 pos = box2d.getBodyPixelCoord(body);
    // Get its angle of rotation
    float a = body.getAngle();
    pushMatrix();
    translate(pos.x, pos.y);
    rotate(-a);
    fill(255);
//     fill(random(175,255),random(175,255),random(175,255));
    stroke(0);
    strokeWeight(1);
    ellipse(0, 0, r*2, r*2);
    //    // Let's add a line so we can see the rotation
    line(0,0,r,0);
    popMatrix();
  }

  // Here's our function that adds the particle to the Box2D world
  public void makeBody(float x, float y, float r) {
    // Define a body
    BodyDef bd = new BodyDef();
    // Set its position
    bd.position = box2d.coordPixelsToWorld(x, y);
    body = box2d.world.createBody(bd);

    // Make the body's shape a circle
    CircleDef cd = new CircleDef();
    cd.radius = box2d.scalarPixelsToWorld(r);
    cd.density = 1.0f;
    cd.friction = 0.01f;
    cd.restitution = 0.3f; // Restitution is bounciness
    body.createShape(cd);

    // Always do this at the end
    body.setMassFromShapes();

    // Give it a random initial velocity (and angular velocity)
    body.setLinearVelocity(new Vec2(random(-10f, 10f), random(5f, 10f)));
    body.setAngularVelocity(random(-10, 10));
  }
  
    // Change color when hit
  public void change() {
    col = color(255,0,0); 
  }
}

// A fixed boundary class

class Boundary {

  // A boundary is a simple rectangle with x,y,width,and height
  float x;
  float y;
  float w;
  float h;
  // But we also have to make a body for box2d to know about it
  Body b;

  Boundary(float x_, float y_, float w_, float h_) {
    x = x_;
    y = y_;
    w = w_;
    h = h_;

    // Figure out the box2d coordinates
    float box2dW = box2d.scalarPixelsToWorld(w/2);
    float box2dH = box2d.scalarPixelsToWorld(h/2);
    Vec2 center = new Vec2(x, y);

    // Define the polygon
    PolygonDef sd = new PolygonDef();
    sd.setAsBox(box2dW, box2dH);
    sd.density = 0;    // No density means it won't move!
    sd.friction = 0.3f;

    // Create the body
    BodyDef bd = new BodyDef();
    bd.position.set(box2d.coordPixelsToWorld(center));
    b = box2d.createBody(bd);
    b.createShape(sd);
  }

  // Draw the boundary, if it were at an angle we'd have to do something fancier
  public void display() {
    fill(0);
    stroke(0);
    rectMode(CENTER);
    rect(x, y, w, h);
  }
}

// ContactListener to listen for collisions!

class CustomListener implements ContactListener {
  CustomListener() {
  }

  // This function is called when a new collision occurs
  public void add(ContactPoint cp) {
    // Get both shapes
    Shape s1 = cp.shape1;
    Shape s2 = cp.shape2;

    
    // Get both bodies
    Body b1 = s1.getBody();
    Body b2 = s2.getBody();

    // Get our objects that reference these bodies
    Object o1 = b1.getUserData();
    Object o2 = b2.getUserData();


    // What class are they?  Box or Ball?
    String c1 = o1.getClass().getName();
    String c2 = o2.getClass().getName();

    //    println(c1);
    // If object 1 is a Box, then object 2 must be a particle
    // Note we are ignoring particle on particle collisions
    if (c1.contains("Paddle")) {
      Ball ball = (Ball) o2;
      ball.change();
    } 
    // If object 2 is a Box, then object 1 must be a particle
    else if (c2.contains("Paddle")) {
      Ball ball = (Ball) o1;
      ball.change();
    }
  }


  // Contacts continue to collide - i.e. resting on each other
  public void persist(ContactPoint cp) {
  }

  // Objects stop touching each other
  public void remove(ContactPoint cp) {
  }

  // Contact point is resolved into an add, persist etc
  public void result(ContactResult cr) {
  }
}






// A rectangular box

class Paddle {

  // We need to keep track of a Body and a width and height
  Body paddle;
  float w;
  float h;

  // Constructor
  Paddle(float x_, float y_) {
    float x = x_;
    float y = y_;
    w =300;
    h =300;
    // Add the box to the box2d world
    makeBody(new Vec2(x, y), w, h);
  }

  // This function removes the particle from the box2d world
  public void killBody() {
    box2d.destroyBody(paddle);
  }

  public boolean contains(float x, float y) {
    Vec2 worldPoint = box2d.coordPixelsToWorld(x, y);
    Shape s = paddle.getShapeList();
    boolean inside = s.testPoint(paddle.getMemberXForm(), worldPoint);
    return inside;
  }

  // Drawing the box
  public void display() {
    // We look at each body and get its screen position
    Vec2 pos = box2d.getBodyPixelCoord(paddle);
    // Get its angle of rotation
    float a = paddle.getAngle();
    rectMode(PConstants.CENTER);
    pushMatrix();
    translate(pos.x, pos.y);
    //    rotate(a);
    //    fill(175);
    noFill();
    stroke(255);
    strokeWeight(2);
    rect(10, 10, w, h);
    popMatrix();
  }

  // This function adds the rectangle to the box2d world
  public void makeBody(Vec2 center, float w_, float h_) {
    // Define and create the body
    BodyDef bd = new BodyDef();
    bd.position.set(box2d.coordPixelsToWorld(center));
    paddle = box2d.createBody(bd);

    // Define the shape -- a polygon (this is what we use for a rectangle)
    PolygonDef sd = new PolygonDef();
    float box2dW = box2d.scalarPixelsToWorld(w_/2);
    float box2dH = box2d.scalarPixelsToWorld(h_/2);
    sd.setAsBox(box2dW, box2dH);
    // Parameters that affect physics
    sd.density = 1.0f;
    sd.friction = 0.3f;
    sd.restitution = 0.5f;

    // Attach that shape to our body!
    paddle.createShape(sd);
    paddle.setMassFromShapes();

    // Give it some initial random velocity
    //    paddle.setLinearVelocity(new Vec2(random(-5,5),random(2,5)));
    //    paddle.setAngularVelocity(random(-5,5));
  }
}


// Class to describe the spring joint (displayed as a line)

class Spring {

  // This is the box2d object we need to create
  MouseJoint mouseJoint;

  Spring() {
    // At first it doesn't exist
    mouseJoint = null;
  }

  // If it exists we set its target to the mouse location 
  public void update(float x, float y) {
    if (mouseJoint != null) {
      // Always convert to world coordinates!
      Vec2 mouseWorld = box2d.coordPixelsToWorld(x,y);
      mouseJoint.setTarget(mouseWorld);
    }
  }

  public void display() {
    if (mouseJoint != null) {
      // We can get the two anchor points
      Vec2 v1 = mouseJoint.getAnchor1();
      Vec2 v2 = mouseJoint.getAnchor2();
      // Convert them to screen coordinates
      v1 = box2d.coordWorldToPixels(v1);
      v2 = box2d.coordWorldToPixels(v2);
      // And just draw a line
      stroke(0);
      strokeWeight(1);
      line(v1.x,v1.y,v2.x,v2.y);
    }
  }


  // This is the key function where
  // we attach the spring to an x,y location
  // and the Box object's location
  public void bind(float x, float y, Paddle paddle) {
    // Define the joint
    MouseJointDef md = new MouseJointDef();
    // Body 1 is just a fake ground body for simplicity (there isn't anything at the mouse)
    md.body1 = box2d.world.getGroundBody();
    // Body 2 is the box's boxy
    md.body2 = paddle.paddle;
    // Get the mouse location in world coordinates
    Vec2 mp = box2d.coordPixelsToWorld(x,y);
    // And that's the target
    md.target.set(mp);
    // Some stuff about how strong and bouncy the spring should be
     md.maxForce = 5000000*paddle.paddle.m_mass;
//    md.maxForce = 1000.0 * paddle.paddle.m_mass;
//    md.frequencyHz = 1.0;
//    md.dampingRatio = 0;

    // Wake up body!
    paddle.paddle.wakeUp();

    // Make the joint!
    mouseJoint = (MouseJoint) box2d.world.createJoint(md);
  }

  public void destroy() {
    // We can get rid of the joint when the mouse is released
    if (mouseJoint != null) {
      box2d.world.destroyJoint(mouseJoint);
      mouseJoint = null;
    }
  }

}


  static public void main(String args[]) {
    PApplet.main(new String[] { "--present", "--bgcolor=#666666", "--stop-color=#cccccc", "_121213_FaceDetection2" });
  }
}
