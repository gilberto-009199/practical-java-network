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

        // SchedulingThreadExample

        // PoolsAndExecutorsThreadExample

    }

}
