/*
 Based on a solution to Barbershop problem given in a blog post by
 Marcelo Magallon: https://blog.ksub.org/bytes/2016/02/14/the-barbershop-problem/.
 This solution uses channels as the main concurrency mechanism.
*/
package main

import (
    "fmt"
    "time"
    "sync"
)

type Customer struct{
  id int
}

type Barbershop struct{
  WaitRoomSize int
  WaitingCh chan bool
  BarbershopCh chan Customer
}

func simulate(numCusts int){
    b := Barbershop{4, make(chan bool), make(chan Customer)}
    barberCh := make(chan Customer, 4) //waiting room seats
    var wait sync.WaitGroup
    wait.Add(1)

    //barber
    go func(){
      for len(barberCh) > 0{
        //c := <- barberCh
        <- barberCh
        //cut hair
        //fmt.Printf("Cutting hair of customer %d\n", c.id)
        time.Sleep(10000000) //0.01 seconds
      }
      wait.Done()
    }()
    //customers
    go func(){
      for i := 0; i < numCusts; i++{
        b.BarbershopCh <- Customer{i}
        <- b.WaitingCh
      }
    }()
    for i := 0; i < numCusts; i++{
      c := <- b.BarbershopCh
      //fmt.Printf("Number of taken chairs: %d\n", len(barberCh))
      switch{
      case len(barberCh) == b.WaitRoomSize:
        //reject customer from waiting room
        //fmt.Printf("Customer %d rejected\n", c.id)
        b.WaitingCh <- false
      default:
        //allow customer into waiting room
        barberCh <- c
        //fmt.Printf("Customer %d waiting\n", c.id)
        b.WaitingCh <- true
      }
    }
    wait.Wait()
}
func main(){
  //correctness testing
  //simulate(30)

  //performance testing
  start := time.Now()
  simulate(20)
  end := time.Now()
  difference := end.Sub(start)
  fmt.Printf("Step 1: Time elapsed (sec) = %f\n", difference.Seconds())

  start = time.Now()
  simulate(200)
  end = time.Now()
  difference = end.Sub(start)
  fmt.Printf("Step 2: Time elapsed (sec) = %f\n", difference.Seconds())

  start = time.Now()
  simulate(2000)
  end = time.Now()
  difference = end.Sub(start)
  fmt.Printf("Step 3: Time elapsed (sec) = %f\n", difference.Seconds())
}
