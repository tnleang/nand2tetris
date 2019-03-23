// This file is part of www.nand2tetris.org
// and the book "The Elements of Computing Systems"
// by Nisan and Schocken, MIT Press.
// File name: projects/04/Fill.asm

// Runs an infinite loop that listens to the keyboard input.
// When a key is pressed (any key), the program blackens the screen,
// i.e. writes "black" in every pixel;
// the screen should remain fully black as long as the key is pressed. 
// When no key is pressed, the program clears the screen, i.e. writes
// "white" in every pixel;
// the screen should remain fully clear as long as no key is pressed.

// Put your code here.
//  while(true)
//      if(key press)
//          fill up the screen with black
//          i++
//      else (when key is not press)
//          clear the screen with white
//          i--

@8192	 // 32 * 256 number of 16 bit pixel lines to cover the entire screen
D=A
@MAX
M=D 
@i
M=0   

(LOOP)
    
    @KBD    //read input from keyboard value = 0 when no key is pressed
    D=M 
    @WHITE  //go to white to clear the screen if no key press
    D; JEQ

(BLACK)
    @MAX 
    D=M 
    @i 
    D=D-M
    @LOOP
    D;JLE  // if i<0 goto LOOP DON'T GO OVER @MAX

    @i
    D=M 
    @SCREEN     //fill black
    A=A+D 
    M=-1

    @i   //i++
    D=M 
    D=D+1
    @i
    M=D 
    @BLACK
    0;JMP

(WHITE)
    @i 
    D=M 
    @SCREEN     //fill white
    A=A+D 
    M=0

    @i 
    D=M 
    @LOOP 
    D; JEQ  //if i == 0 jump to loop

    @i   //i--
    D=M 
    D=D-1
    @i 
    M=D 

    @WHITE 
    0; JMP 

