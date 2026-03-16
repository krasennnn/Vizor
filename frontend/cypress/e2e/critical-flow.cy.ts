/// <reference types="cypress" />

describe('Critical Flow E2E Test', () => {
  // Helper function to generate unique test users
  const getUniqueUser = (suffix: number = 0) => {
    const timestamp = Date.now()
    const random = Math.floor(Math.random() * 10000) // Add randomness
    return {
      email: `test${timestamp}${suffix}${random}@cypress.test`,
      username: `testuser${timestamp}${suffix}${random}`,
      password: 'testpassword123'
    }
  }

  let ownerCreatorUser: { email: string; username: string; password: string }
  let creatorOnlyUser: { email: string; username: string; password: string }
  let campaignName: string

  beforeEach(() => {
    // Generate unique users for each test run with different suffixes
    ownerCreatorUser = getUniqueUser(1)
    creatorOnlyUser = getUniqueUser(2)
    campaignName = `Test Campaign ${Date.now()}`
  })

  it('should complete the full critical flow: register owner+creator, create campaign, verify cannot apply, register creator, apply, accept contract, verify status', () => {
    // Step 1: Register user with both owner and creator roles
    cy.visit('/register')
    cy.get('input[placeholder*="john@example.com"]').clear().type(ownerCreatorUser.email)
    cy.get('input[placeholder*="john_doe"]').clear().type(ownerCreatorUser.username)
    cy.get('input[type="password"]').clear().type(ownerCreatorUser.password)
    
    // Select both Creator and Owner roles
    cy.contains('Creator').parent().parent().click()
    cy.wait(500) // Wait for state update
    cy.contains('Owner').parent().parent().click()
    cy.wait(500) // Wait for state update
    
    // Wait for button to become enabled
    cy.get('button').contains('Create Account').should('be.enabled')
    cy.get('button').contains('Create Account').click()
    
    // Verify redirect to login page and success message
    cy.url({ timeout: 10000 }).should('include', '/login')
    cy.get('[role="alert"]', { timeout: 10000 }).should('be.visible')
    cy.contains('Success').should('be.visible')
    cy.wait(2000) // Wait for alert to be visible

    // Step 2: Login with the owner+creator account
    cy.get('input[placeholder*="john@example.com"]').type(ownerCreatorUser.email)
    cy.get('input[type="password"]').type(ownerCreatorUser.password)
    cy.contains('Sign In').click()
    
    cy.url().should('eq', 'http://localhost:5173/')
    cy.get('[role="alert"]', { timeout: 3000 }).should('be.visible')
    cy.contains('Success').should('be.visible')
    cy.wait(2000) // Wait for alert

    // Step 3: Go to campaign page
    cy.contains('Campaigns').click()
    cy.url().should('include', '/campaign')
    cy.wait(1000) // Wait for page to load

    // Step 4: Click create campaign and generate one
    cy.contains('Create Campaign').click()
    cy.wait(1000) // Wait for dialog to open
    
    // Fill in campaign form
    cy.get('input[placeholder*="Summer Launch"]').type(campaignName)
    cy.wait(500)
    
    // The form has default start date (today), so we can just submit
    // The end date is optional, so we'll leave it empty
    cy.get('button[type="submit"]').contains('Create Campaign').click()
    
    // Wait for campaign to be created
    cy.wait(1000) // Wait for API call and dialog to close
    // Verify success alert appears - the title is "Campaign created successfully"
    cy.get('[role="alert"]', { timeout: 5000 }).should('be.visible')
    cy.contains('Campaign created successfully').should('be.visible')
    cy.wait(2000) // Wait for alert
    
    // Verify campaign appears in the list
    cy.contains(campaignName).should('be.visible')

    // Step 5: Go back to home page
    cy.contains('Home').click()
    cy.url().should('eq', 'http://localhost:5173/')
    cy.wait(1000) // Wait for page to load

    // Step 6: Try to apply to the campaign - should NOT work, should say "Your Campaign"
    // First, search for the campaign to find it
    cy.get('input[placeholder*="Search campaigns"]').type(campaignName)
    cy.wait(1500) // Wait for search to filter
    
    // Verify the campaign shows "Your Campaign" button (disabled)
    cy.contains(campaignName).should('be.visible')
    // Find the card containing the campaign name and check for "Your Campaign" button
    // The campaign name is in a card, and the button should be in the same card
    cy.contains(campaignName).closest('div[class*="card"], div[class*="Card"]').within(() => {
      // Find the span with "Your Campaign" text, then get its parent button
      cy.contains('span', 'Your Campaign').parent('button').should('be.disabled')
    })
    
    // Step 7: Verify search filter works
    cy.get('input[placeholder*="Search campaigns"]').clear()
    cy.wait(500)
    cy.get('input[placeholder*="Search campaigns"]').type(campaignName)
    cy.wait(1500)
    cy.contains(campaignName).should('be.visible')

    // Step 8: Log out
    // Click on the user menu button (dropdown trigger with user icon)
    // The button contains a span with "User menu" text (sr-only)
    cy.get('span.sr-only').contains('User menu').parent('button').click()
    cy.wait(500)
    cy.contains('Logout').click()
    cy.wait(2000) // Wait for logout to complete
    cy.url().should('include', '/login')

    // Step 9: Create a new account with creator role ONLY
    cy.contains('Create one').click()
    cy.url().should('include', '/register')
    
    cy.get('input[placeholder*="john@example.com"]').clear().type(creatorOnlyUser.email)
    cy.get('input[placeholder*="john_doe"]').clear().type(creatorOnlyUser.username)
    cy.get('input[type="password"]').clear().type(creatorOnlyUser.password)
    
    // Select only Creator role
    cy.contains('Creator').parent().parent().click()
    cy.wait(500)
    
    // Wait for button to become enabled
    cy.get('button').contains('Create Account').should('be.enabled')
    cy.get('button').contains('Create Account').click()
    
    // Verify redirect to login page
    cy.url({ timeout: 10000 }).should('include', '/login')
    cy.get('[role="alert"]', { timeout: 10000 }).should('be.visible')
    cy.contains('Success').should('be.visible')
    cy.wait(2000)

    // Step 10: Login with creator-only account
    cy.get('input[placeholder*="john@example.com"]').type(creatorOnlyUser.email)
    cy.get('input[type="password"]').type(creatorOnlyUser.password)
    cy.contains('Sign In').click()
    
    cy.url().should('eq', 'http://localhost:5173/')
    cy.get('[role="alert"]', { timeout: 3000 }).should('be.visible')
    cy.contains('Success').should('be.visible')
    cy.wait(2000)

    // Step 11: Go to main page and click apply
    cy.url().should('eq', 'http://localhost:5173/')
    cy.wait(1500) // Wait for page to load
    
    // Search for the campaign
    cy.get('input[placeholder*="Search campaigns"]').type(campaignName)
    cy.wait(1500) // Wait for search to filter
    
    // Click Apply Now button - find it within the campaign card
    cy.contains(campaignName).should('be.visible')
    cy.contains('Apply Now').click()
    cy.wait(1500) // Wait for dialog to open

    // Step 12: Fill in the popup with data (leave default 30 posts)
    // The form should have default 30 posts, we can just submit
    // Or we can verify the default is 30 and submit
    cy.get('input[placeholder*="0.00"]').should('be.visible') // Retainer field (optional)
    // The expected posts field should have default value of 30
    cy.get('input[type="text"]').filter((_, el) => {
      const placeholder = el.getAttribute('placeholder')
      return placeholder === '30' || placeholder === String(30)
    }).should('have.value', '30')
    
    // Submit the application
    cy.get('button[type="submit"]').contains('Send Application').click()
    
    // Wait for application to be submitted
    cy.wait(1000) // Wait for API call and redirect
    cy.get('[role="alert"]', { timeout: 5000 }).should('be.visible')
    cy.contains('successfully').should('be.visible')
    // The dialog should close and redirect to contracts page
    cy.url({ timeout: 1000 }).should('include', '/contracts')
    cy.wait(2000)
    
    // Verify the application appears in the contracts list (Sent tab)
    cy.contains('Sent').should('be.visible')
    cy.contains(campaignName, { timeout: 5000 }).should('be.visible')
    cy.contains('Pending').should('be.visible')

    // Step 13: Log out
    // Click on the user menu button (dropdown trigger with user icon)
    // The button contains a span with "User menu" text (sr-only)
    cy.get('span.sr-only').contains('User menu').parent('button').click()
    cy.wait(500)
    cy.contains('Logout').click()
    cy.wait(2000) // Wait for logout to complete
    cy.url().should('include', '/login')

    // Step 14: Log in with first account (owner + creator)
    cy.get('input[placeholder*="john@example.com"]').type(ownerCreatorUser.email)
    cy.get('input[type="password"]').type(ownerCreatorUser.password)
    cy.contains('Sign In').click()
    
    cy.url().should('eq', 'http://localhost:5173/')
    cy.get('[role="alert"]', { timeout: 3000 }).should('be.visible')
    cy.contains('Success').should('be.visible')
    cy.wait(2000)

    // Step 15: Go to contracts page
    cy.contains('Contracts').click()
    cy.url().should('include', '/contracts')
    cy.wait(2000) // Wait for contracts to load

    // Step 16: Accept the request
    // The contract should be in the "Received" tab with "Pending" status
    // Check if we're already on Received tab, if not, click it
    cy.get('button').contains('Received').then(($btn) => {
      if (!$btn.hasClass('bg-primary')) {
        cy.wrap($btn).click()
        cy.wait(1000)
      }
    })
    
    // Find the contract for the campaign and click Accept
    cy.contains(campaignName).should('be.visible')
    // Find the Accept button in the same table row as the campaign
    cy.contains(campaignName).closest('tr').within(() => {
      cy.contains('Accept').click()
    })
    cy.wait(2000) // Wait for API call
    cy.get('[role="alert"]', { timeout: 5000 }).should('be.visible')
    cy.contains('successfully').should('be.visible')
    cy.wait(2000)
    
    // Verify contract status changed from Pending to Accepted
    cy.contains(campaignName).closest('tr').within(() => {
      cy.contains('Accepted').should('be.visible')
    })

    // Step 17: Go to campaign page
    cy.contains('Campaigns').click()
    cy.url().should('include', '/campaign')
    cy.wait(1000)

    // Step 18: Click on "View Analytics" button for that campaign
    // The button is rendered as a Link (anchor tag) due to asChild prop
    // Find the campaign name first to ensure it's loaded, then find the link
    cy.contains(campaignName).should('be.visible')
    // Find the link that contains "View Analytics" - it should be unique on the page
    cy.get('a').contains('View Analytics').click()
    cy.url({ timeout: 10000 }).should('include', '/campaign/')
    cy.wait(2000) // Wait for campaign detail page to load

    // Step 19: Should see entry in creator and contracts
    cy.contains('Creators & Contracts').should('be.visible')
    // The creator username should be visible in the table
    cy.contains(creatorOnlyUser.username, { timeout: 5000 }).should('be.visible')
    cy.contains('Accepted').should('be.visible')
    
    // Verify the contract shows expected posts (30)
    cy.contains(creatorOnlyUser.username).closest('tr').within(() => {
      cy.contains('30').should('be.visible') // Expected posts
    })

    // Step 20: Click on "View" button in the contracts table
    // Find the View button in the same row as the creator username
    cy.contains(creatorOnlyUser.username).closest('tr').within(() => {
      cy.contains('View').click()
    })
    cy.url({ timeout: 10000 }).should('include', '/contracts/')
    cy.wait(2000) // Wait for contract detail page to load

    // Step 21: Should see it's at stage "work in progress"
    // Verify "Work in Progress" is visible and has "Current" badge aligned to the right
    // The structure: div.flex.justify-between > div (with Work in Progress) + Badge (Current)
    cy.contains('Work in Progress').should('be.visible')
    // Find the parent flex container and verify "Current" badge is in the same row
    cy.contains('Work in Progress').parents('div.flex.items-center.justify-between').first().within(() => {
      cy.contains('Current').should('be.visible')
    })
    
    // Step 22: Verify Campaign Information section is correct
    cy.contains('Campaign Information').parent().parent().within(() => {
      // Verify campaign name matches
      cy.contains(campaignName).should('be.visible')
      // Verify creator username is shown (since we're viewing as owner)
      cy.contains(creatorOnlyUser.username).should('be.visible')
      // Verify start date is shown and matches today's date
      // Format the date the same way formatDate does: "en-US" locale with short month
      const todayFormatted = new Date().toLocaleDateString("en-US", {
        month: "short",
        day: "numeric",
        year: "numeric",
      })
      cy.contains(todayFormatted).should('be.visible')
    })
  })
})
