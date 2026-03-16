/// <reference types="cypress" />

describe('Login Flow', () => {
  beforeEach(() => {
    cy.visit('/login')
  })

  // Tests that a user can successfully log in with valid credentials
  // Verifies redirect to home page and success alert display
    it('should login successfully with valid credentials', () => {
      cy.get('input[placeholder*="john@example.com"]').type('test@example.com')
      cy.get('input[type="password"]').type('testpassword123')
      cy.contains('Sign In').click()
      
      cy.url().should('eq', 'http://localhost:5173/')
      cy.get('[role="alert"]', { timeout: 3000 }).should('be.visible')
      cy.contains('Success').should('be.visible')
    })
  
    // Tests that login fails with a non-existent email address
    // Verifies error alert is displayed to the user
    it('should show error for invalid email', () => {
      cy.get('input[placeholder*="john@example.com"]').type('wrong@test.com')
      cy.get('input[type="password"]').type('wrongpassword')
      cy.contains('Sign In').click()
      
      cy.get('[role="alert"]', { timeout: 3000 }).should('be.visible')
      cy.contains('Error').should('be.visible')
    })
  
    // Tests that login fails with correct email but wrong password
    // Verifies error alert is displayed to the user
    it('should show error for wrong password', () => {
      cy.get('input[placeholder*="john@example.com"]').type('test@example.com')
      cy.get('input[type="password"]').type('wrongpassword')
      cy.contains('Sign In').click()
      
      cy.get('[role="alert"]', { timeout: 3000 }).should('be.visible')
      cy.contains('Error').should('be.visible')
    })
  
    // Tests navigation from login page to register page
    // Verifies the "Create one" link works correctly
    it('should navigate to register page', () => {
      cy.contains('Create one').click()
      cy.url().should('include', '/register')
    })
  })
  
  describe('Register Flow', () => {
    beforeEach(() => {
      cy.visit('/register')
    })
  
    // Helper function to generate unique test users
    // Prevents database conflicts by using timestamps
    const getUniqueUser = () => {
      const timestamp = Date.now()
      return {
        email: `test${timestamp}@cypress.test`,
        username: `testuser${timestamp}`,
        password: 'testpassword123'
      }
    }
  
    // Tests successful registration as a Creator role
    // Verifies form submission, redirect to login, and success alert
    it('should register successfully as Creator', () => {
      const user = getUniqueUser()
      
      cy.get('input[placeholder*="john@example.com"]').clear().type(user.email)
      cy.get('input[placeholder*="john_doe"]').clear().type(user.username)
      cy.get('input[type="password"]').clear().type(user.password)
      
      // Select Creator role
      cy.contains('Creator').parent().parent().click()
      
      // Wait for button to become enabled (state update)
      cy.get('button').contains('Create Account').should('not.be.disabled')
      cy.get('button').contains('Create Account').should('be.enabled')
      
      // Submit the form
      cy.get('button').contains('Create Account').click()
      
      // Verify redirect to login page and success message
      cy.url({ timeout: 10000 }).should('include', '/login')
      cy.get('[role="alert"]', { timeout: 10000 }).should('be.visible')
      cy.contains('Success').should('be.visible')
    })
  
    // Tests successful registration as an Owner role
    // Verifies form submission, redirect to login, and success alert
    it('should register successfully as Owner', () => {
      const user = getUniqueUser()
      
      cy.get('input[placeholder*="john@example.com"]').clear().type(user.email)
      cy.get('input[placeholder*="john_doe"]').clear().type(user.username)
      cy.get('input[type="password"]').clear().type(user.password)
      
      // Select Owner role
      cy.contains('Owner').parent().parent().click()
      
      // Wait for button to become enabled
      cy.get('button').contains('Create Account').should('not.be.disabled')
      cy.get('button').contains('Create Account').should('be.enabled')
      
      // Submit the form
      cy.get('button').contains('Create Account').click()
      
      // Verify redirect to login page and success message
      cy.url({ timeout: 10000 }).should('include', '/login')
      cy.get('[role="alert"]', { timeout: 10000 }).should('be.visible')
      cy.contains('Success').should('be.visible')
    })
  
    // Tests that form validation prevents submission when no role is selected
    // Verifies button is disabled and error message is displayed
    it('should show error when no role is selected', () => {
      const user = getUniqueUser()
      
      cy.get('input[placeholder*="john@example.com"]').clear().type(user.email)
      cy.get('input[placeholder*="john_doe"]').clear().type(user.username)
      cy.get('input[type="password"]').clear().type(user.password)
      
      // Don't select any role - verify button is disabled
      cy.get('button').contains('Create Account').should('be.disabled')
      
      // Verify error message appears below role selection
      cy.contains('Please select at least one role').should('be.visible')
    })
  
    // Tests form validation for invalid email format
    // Verifies that email validation error is displayed when submitting invalid email
    it('should show error for invalid email format', () => {
        // Use email that passes HTML5 validation (has @) but fails Zod validation
        cy.get('input[placeholder*="john@example.com"]').clear().type('test@', { force: true })
        cy.get('input[placeholder*="john_doe"]').clear().type('testuser')
        cy.get('input[type="password"]').clear().type('testpassword123')
        cy.contains('Creator').parent().parent().click()
        
        cy.get('button').contains('Create Account').click()
        
        // Verify Zod validation error message appears (this one is visible in DOM)
        cy.contains('Email must be valid', { timeout: 2000 }).should('be.visible')
    })
  
    // Tests form validation for password that is too short
    // Verifies that password validation error is displayed when password is less than 8 characters
    it('should show error for short password', () => {
      const user = getUniqueUser()
      
      cy.get('input[placeholder*="john@example.com"]').clear().type(user.email)
      cy.get('input[placeholder*="john_doe"]').clear().type(user.username)
      cy.get('input[type="password"]').clear().type('short')
      cy.contains('Creator').parent().parent().click()
      
      cy.get('button').contains('Create Account').click()
      
      // Verify password validation error message appears
      cy.contains('Password must be at least 8 characters', { timeout: 2000 }).should('be.visible')
    })
  
    // Tests that registration fails when trying to use an email that already exists
    // Verifies error alert is displayed when duplicate email is submitted
    it('should show error for duplicate email', () => {
      // Try to register with existing email
      cy.get('input[placeholder*="john@example.com"]').clear().type('test@example.com')
      cy.get('input[placeholder*="john_doe"]').clear().type('newusername')
      cy.get('input[type="password"]').clear().type('testpassword123')
      cy.contains('Creator').parent().parent().click()
      
      // Wait for button to be enabled
      cy.get('button').contains('Create Account').should('be.enabled')
      cy.get('button').contains('Create Account').click()
      
      // Verify error alert appears after API call
      cy.get('[role="alert"]', { timeout: 5000 }).should('be.visible')
      cy.contains('Error', { timeout: 2000 }).should('be.visible')
    })
  
    // Tests navigation from register page to login page
    // Verifies the "Sign in" link works correctly
    it('should navigate to login page', () => {
      cy.contains('Sign in').click()
      cy.url().should('include', '/login')
    })
  })