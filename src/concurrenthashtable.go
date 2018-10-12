/*
 * Using implementation of concurrent hash table provided in concurrenthashmap
 * package in Flo(w)Rest framework:
 * https://godoc.org/github.com/jabong/florest-core/src/common/collections/maps/concurrentmap/concurrenthashmap
 */

package main

import (
    "github.com/jabong/florest-core/src/common/collections/maps/concurrentmap/concurrenthashmap"
    "strings"
    "fmt"
    "io/ioutil"
    "sync"
    "time"
)

func check(e error) {
  if e != nil {
      panic(e)
  }
}

func test1(numRoutines int){
  //create table
  concMap := concurrenthashmap.New()
  //get input data
  data, err := ioutil.ReadFile("strinputdata.txt")
  check(err)
  inputs := strings.Split(string(data), " ")

  var wait sync.WaitGroup
  wait.Add(numRoutines)

  for i := 0; i < numRoutines; i++{
    if i % 2 == 0{
      //writer
      go func(mp *concurrenthashmap.Map, k int, v string){
        mp.Put(k,v)
        wait.Done()
      }(concMap, i, inputs[i])
    }else{
      //reader
      go func(mp *concurrenthashmap.Map, k int){
        _, found := mp.Get(k)
        if !found{
          //fmt.Printf("read with key %d into empty entry\n", k)
        }else{
          /*str, ok := val.(string)
          if ok{
            fmt.Printf("read with key %d returned string '%s'\n", k, str)
          }*/

        }
        wait.Done()
      }(concMap, i)
    }
  }
  wait.Wait()
}

func main(){
  //correctness testing
  //test1(50)

  //performance testing
  start := time.Now()
  test1(20)
  end := time.Now()
  difference := end.Sub(start)
  fmt.Printf("Step 1: Time elapsed (sec) = %f\n", difference.Seconds())

  start = time.Now()
  test1(200)
  end = time.Now()
  difference = end.Sub(start)
  fmt.Printf("Step 2: Time elapsed (sec) = %f\n", difference.Seconds())

  start = time.Now()
  test1(2000)
  end = time.Now()
  difference = end.Sub(start)
  fmt.Printf("Step 3: Time elapsed (sec) = %f\n", difference.Seconds())
}
