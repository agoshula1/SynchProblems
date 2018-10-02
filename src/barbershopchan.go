/*
 Based on a solution to Barbershop problem given in a blog post by
 Marcelo Magallon: https://blog.ksub.org/bytes/2016/02/14/the-barbershop-problem/.
 This solution uses channels as the main concurrency mechanism.
*/
package main

import (
    "fmt"
    "time"
)

type Customer struct{
  id int
}

type Barbershop struct{
  WaitRoomSize int
  WaitingCh chan bool
  BarbershopCh chan Customer
}

func main(){
  b := Barbershop{4, make(chan bool), make(chan Customer)}
  barberCh := make(chan Customer, 4) //waiting room seats
  //barber
  go func(){
    for{
      <- barberCh
      //cut hair
      time.Sleep(1)
    }
  }()
  //customer
  go func(){
    i := 0
    for{
      b.BarbershopCh <- Customer{i}
      <- b.WaitingCh
      i++
    }
  }()
  for{
    c := <- b.BarbershopCh
    switch{
    case len(barberCh) == b.WaitRoomSize:
      //reject customer from waiting room
      b.WaitingCh <- false
    default:
      //allow customer into waiting room
      barberCh <- c
      fmt.Printf("Customer %d waiting", c.id)
      b.WaitingCh <- true
    }
  }
}
