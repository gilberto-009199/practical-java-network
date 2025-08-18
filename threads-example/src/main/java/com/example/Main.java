package com.example;

public class Main {

    public static void main( String[] args ){

        // ThreadsAndRunnableExample
        new ThreadsAndRunnableExample().example();

        // ReturningProcessThreadExample
        new ReturningProcessThreadExample().example();
        new CallableAndExecutorExample().example();

        // SyncronizationThreadExample
        new SyncronizationThreadExample().example();
        // @todo add example use atomic


        // DeadLockThreadExample
        new DeadLockThreadExample().example();
        new DeadLockSolutionExample().example();
        // @todo add 3° component for lock, dependencie cicle

        // SchedulingThreadExample
        new SchedulingThreadExample().example();

    }

}
