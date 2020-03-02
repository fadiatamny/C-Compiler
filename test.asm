SECTION .TEXT
GLOBAL _start
_start:
 call main
 mov eax, 1
 xor ebx, ebx
 int 0x80

printChar:
 push rbp
 mov rbp, rsp
 push rdi
 mov byte [rbp - 5], 0x41
 mov byte [rbp - 4], 0x53
 mov byte [rbp - 3], 0x41
 mov byte [rbp - 2], 0x46
 mov byte[rbp - 1], 0
 mov rax, 1
 mov rdi, 1
 lea rsi, [rbp -5]
 mov rdx, 5
 syscall 

 mov rsp, rbp
 pop rbp
 ret

printNumber:
push rbp
 mov rbp, rsp
 mov rsi, rdi
 lea rdi, [rbp - 1]
 mov byte [rdi], 0
 mov rax, rsi
 while:
 cmp rax, 0
 je done
 mov rcx, 10
 mov rdx, 0
 div rcx
 dec rdi
 add dl, 0x30
 mov byte [rdi], dl
 jmp while

 done:
 mov rax, 1
 lea rsi, [rdi]
 mov rdx, rsp
sub rdx, rsi
 mov rdi, 1
 syscall 

 mov rsp, rbp
 pop rbp
 ret

readInteger:
 push rbp
 mov rbp, rsp

 mov rdx, 10
 mov qword [rbp - 10], 0
 mov word [rbp - 2], 0
 lea rsi, [rbp- 10]
 mov rdi, 0 ; stdin
 mov rax, 0 ; sys_read
 syscall

 xor rax, rax
 xor rbx, rbx
 lea rcx, [rbp - 10]
 
 copy_byte:
cmp rbx, 10
 je read_done 
 mov dl, byte [rcx]
 cmp dl, 10
 jle read_done
 sub rdx, 0x30
 imul rax, 10
 add rax, rdx
 nextchar:
 inc rcx
 inc rbx
 jmp copy_byte
 read_done:
 mov rsp, rbp
 pop rbp
 ret


f:
push rbp
mov rbp, rsp
mov eax, 5
push rax
pop rax
mov dword [rbp - 8], eax
mov eax, dword [rbp - 8]
push rax
mov rsp, rbp
pop rbp
ret
main:
push rbp
mov rbp, rsp
mov eax, 5
push rax
pop rax
mov dword [rbp - 12], eax
mov eax, 5
push rax
whileStart0:
mov eax, dword [rbp - 12]
push rax
pop rax
mov rbx, rax
pop rax
cmp rax, rbx
jge true0
push 1
jmp compdone0
true0:
push 0
compdone0:
mov eax, dword [rbp - 12]
push rax
mov eax, 8
push rax
pop rax
cmp rax, 1
jne compdone1
push rax
mov eax, 16
push rax
pop rax
mov dword [rbp - 16], eax
mov eax, dword [rbp - 12]
push rax
mov eax, 1
push rax
pop rbx
pop rax
add rax, rbx
push rax
jmp whileStart0
compdone1:
mov eax, 0
push rax
mov rsp, rbp
pop rbp
ret
