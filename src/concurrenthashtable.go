package main

import (
    "github.com/jabong/florest-core/src/common/collections/maps/concurrentmap/concurrenthashmap"
    "strings"
    "fmt"
    "io/ioutil"
    "sync"
)

func check(e error) {
  if e != nil {
      panic(e)
  }
}

func main(){
  //create table
  concMap := concurrenthashmap.New()
  //get input data
  data, err := ioutil.ReadFile("strinputdata.txt")
  check(err)
  inputs := strings.Split(string(data), " ")

  var wait sync.WaitGroup
  rwRoutines := 20
  wait.Add(rwRoutines*2)
  for i := 0; i < rwRoutines; i++{
    //writer
    go func(mp *concurrenthashmap.Map, k int, v string){
      mp.Put(k,v)
      wait.Done()
    }(concMap, i, inputs[i])

    //reader
    go func(mp *concurrenthashmap.Map, k int){
      _, found := mp.Get(k)
      if !found{
        fmt.Printf("read with key %d into empty entry\n", k)
      }else{
        //fmt.Printf("read with key %d returned string '%s'\n", k, val)
      }
      wait.Done()
    }(concMap, i)

  }
  wait.Wait()
}
