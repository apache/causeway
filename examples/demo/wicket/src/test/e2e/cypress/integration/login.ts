describe('login to demo application', () => {

    it('login', () => {
        cy.visit('/wicket/');
        cy.get('.isis-username')
            .type('sven')
            // @ts-ignore
            .tab();
        cy.get('.isis-password')
            .type('pass')
            .type('{enter}');

        cy.get('.entityTitle').contains('Demo Home Page')
    });

})
