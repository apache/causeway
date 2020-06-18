describe('learning typescript', () => {

    it('should perform basic google search', () => {
        cy.visit('https://google.com');
        cy.get('[name="q"]')
            .type('subscribe')
            .type('{enter}');
    });

})
