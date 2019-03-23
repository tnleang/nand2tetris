// This file is part of www.nand2tetris.org
// and the book "The Elements of Computing Systems"
// by Nisan and Schocken, MIT Press.
// File name: projects/04/Mult.asm

// Multiplies R0 and R1 and stores the result in R2.
// (R0, R1, R2 refer to RAM[0], RAM[1], and RAM[2], respectively.)

// Put your code here.
@sum
M=0     //sum=0
@i
M=0     //i=0
        //R1*R0
        //loop: repeat R0 times sum+=R1
(Loop)
@i
D=M
@R0
D=D-M  //D=i-R0
@End
D;JEQ   //if(i == R0 ) end
@R1
D=M     //D=R1
@sum
M=M+D   //sum+=R1
@i
M=M+1  //i++
@Loop
0;JMP
(End)
@sum
D=M
@R2
M=D

