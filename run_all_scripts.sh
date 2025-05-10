#!/bin/bash
###############################################################################
# Project Setup Instructions - Manual Step by Step Guide
###############################################################################

# This file contains instructions for manually setting up the entire project.
# Instead of running scripts, follow these steps in order to build the project.

# PROJECT OVERVIEW
# ---------------
# This project implements a retail management system with the following components:
# - Product hierarchy with specialized types (Food/NonFood)
# - Store configuration for pricing, discounts, and markups
# - Receipts generation system
# - Exception handling
# - Various entity classes (Person, Customer, etc.)

# MANUAL SETUP INSTRUCTIONS
# -----------------------

# 1. SETUP BASIC PROJECT STRUCTURE (Day 1)
# --------------------------------------
# See day1_corrected.sh for detailed instructions on:
#  - Creating project directory and initializing Git
#  - Setting up Gradle build configuration
#  - Creating basic entity classes (Person, GoodsType)

# 2. IMPLEMENT PRODUCT HIERARCHY AND STORE CONFIG (Day 2)
# ----------------------------------------------------
# See day2_corrected.sh for detailed instructions on:
#  - Creating StoreConfig with markup and discount settings
#  - Building Product hierarchy with abstract base class
#  - Implementing specialized product classes (FoodProduct, NonFoodProduct)
#  - Writing unit tests for pricing logic

# 3. ADD CUSTOMER, EMPLOYEE AND SERVICE LAYERS (Day 3)
# -------------------------------------------------
# See day3_corrected.sh for detailed instructions on:
#  - Adding Customer and Employee classes extending Person
#  - Creating service interfaces and implementations
#  - Implementing Store class for central product management

# 4. IMPLEMENT PURCHASE AND RECEIPT FUNCTIONALITY (Day 4)
# ---------------------------------------------------
# See day4_corrected.sh for detailed instructions on:
#  - Creating Receipt class for purchase tracking
#  - Implementing receipt generation functionality
#  - Adding unit tests for receipt calculations

# 5. IMPLEMENT EXCEPTION HANDLING (Day 5+)
# -------------------------------------
# See day5_corrected.sh through day12_corrected.sh for:
#  - Adding custom exceptions
#  - Improving error handling throughout the codebase
#  - Enhancing service implementations
#  - Adding more complex business logic

# VERIFICATION STEPS
# ----------------
# After completing each section, verify your progress with:
#  - ./gradlew build
#  - ./gradlew test

# TROUBLESHOOTING
# -------------
# If you encounter issues during setup:
#  - Check the fix_exceptions.sh file for common fixes
#  - Verify package names match the expected structure
#  - Ensure all required dependencies are correctly defined in build.gradle.kts

###############################################################################
# NOTE: This is a manual instruction guide, not a runnable script.
# Follow each referenced file (day1_corrected.sh, day2_corrected.sh, etc.)
# for the detailed steps to build each component of the system.
############################################################################### 