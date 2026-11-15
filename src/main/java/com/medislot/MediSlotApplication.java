package com.medislot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * MediSlot - Hospital Appointment & Patient Care Coordination System
 * Full-Stack OOPs Academic Case Study Project
 */
@SpringBootApplication
@EnableTransactionManagement
public class MediSlotApplication {

    public static void main(String[] args) {
        SpringApplication.run(MediSlotApplication.class, args);
    }
}
