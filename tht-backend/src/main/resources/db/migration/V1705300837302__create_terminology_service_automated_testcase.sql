--create TS automated testcases.
--
--@author dhruv
--@since 2023-09-13
INSERT INTO component (id,
                       name,
                       description,
                       state,
                       rank,
                       created_by,
                       updated_by,
                       created_at,
                       updated_at,
                       version)
VALUES ('component.terminology.service',
        'Terminology Service (TS)',
        'Terminology Service Component (https://guides.ohie.org/arch-spec/openhie-component-specifications-1/openhie-terminology-service-ts)',
        'component.status.active',
        10,
        'SYSTEM_USER',
        'SYSTEM_USER',
        Now(),
        Now(),
        0);


INSERT INTO specification (id,
                           name,
                           description,
                           state,
                           rank,
                           is_functional,
                           is_required,
                           component_id,
                           created_by,
                           updated_by,
                           created_at,
                           updated_at,
                           version)
VALUES ('specification.ts.tswf.1',
        'TSWF-1',
        'TSWF-1 Specification of the Terminology Service Component (https://guides.ohie.org/arch-spec/introduction/terminology-service-workflow/verify-code-existence)',
        'specification.status.active',
        1,
        false,
        true,
        'component.terminology.service',
        'SYSTEM_USER',
        'SYSTEM_USER',
        Now(),
        Now(),
        0),
       ('specification.ts.tswf.2',
        'TSWF-2',
        'TSWF-2 Specification of the Terminology Service Component (https://guides.ohie.org/arch-spec/introduction/terminology-service-workflow/verify-code-membership)',
        'specification.status.active',
        2,
        false,
        true,
        'component.terminology.service',
        'SYSTEM_USER',
        'SYSTEM_USER',
        Now(),
        Now(),
        0),

       ('specification.ts.tswf.3',
        'TSWF-3',
        'TSWF-3 Specification of the Terminology Repository Component (https://guides.ohie.org/arch-spec/introduction/terminology-service-workflow/expand-value-set)',
        'specification.status.active',
        3,
        false,
        true,
        'component.terminology.service',
        'SYSTEM_USER',
        'SYSTEM_USER',
        Now(),
        Now(),
        0),
       ('specification.ts.tswf.4',
        'TSWF-4',
        'TSWF-4 Specification of the Terminology Service Component (https://guides.ohie.org/arch-spec/introduction/terminology-service-workflow/query-concept-maps)',
        'specification.status.active',
        4,
        false,
        true,
        'component.terminology.service',
        'SYSTEM_USER',
        'SYSTEM_USER',
        Now(),
        Now(),
        0),

       ('specification.ts.tswf.5',
        'TSWF-5',
        'TSWF-5 Specification of the Terminology Service Component (https://guides.ohie.org/arch-spec/introduction/terminology-service-workflow/query-code-systems)',
        'specification.status.active',
        5,
        false,
        true,
        'component.terminology.service',
        'SYSTEM_USER',
        'SYSTEM_USER',
        Now(),
        Now(),
        0),

       ('specification.ts.tswf.6',
        'TSWF-6',
        'TSWF-6 Specification of the Terminology Service Component (https://guides.ohie.org/arch-spec/introduction/terminology-service-workflow/query-value-set)',
        'specification.status.active',
        6,
        false,
        true,
        'component.terminology.service',
        'SYSTEM_USER',
        'SYSTEM_USER',
        Now(),
        Now(),
        0),
       ('specification.ts.tswf.7',
        'TSWF-7',
        'TSWF-7 Specification of the Terminology Repository Component (https://guides.ohie.org/arch-spec/introduction/terminology-service-workflow/lookup-code)',
        'specification.status.active',
        7,
        false,
        true,
        'component.terminology.service',
        'SYSTEM_USER',
        'SYSTEM_USER',
        Now(),
        Now(),
        0),
       ('specification.ts.tswf.8',
        'TSWF-8',
        'TSWF-8 Specification of the Terminology Repository Component (https://guides.ohie.org/arch-spec/introduction/terminology-service-workflow/expand-value-set)',
        'specification.status.active',
        8,
        false,
        true,
        'component.terminology.service',
        'SYSTEM_USER',
        'SYSTEM_USER',
        Now(),
        Now(),
        0);

INSERT INTO testcase (id,
                      name,
                      description,
                      state,
                      rank,
                      is_manual,
                      test_suite_id,
                      specification_id,
                      created_by,
                      updated_by,
                      created_at,
                      updated_at,
                      version,
                      test_case_run_environment,
                      sut_actor_api_key)
VALUES ('testcase.ts.tswf.1.1',
        'Verify Code Existence',
        'Testcase to Verify Code Existence for specification TSWF1 of the terminology service',
        'testcase.status.active',
        1,
        false,
        'testcase.ts.tswf.1.1~Verify Code Existence~0',
        'specification.ts.tswf.1',
        'SYSTEM_USER',
        'SYSTEM_USER',
        Now(),
        Now(),
        0,
        'testcase.run.environment.eu.testbed','D6A58024X3EFDX4B2DX8C81X461719B08B66'),
       ('testcase.ts.tswf.2.1',
        'Verify Code Membership',
        'Testcase to verify code membership for specification TSWF2 of the terminology repository',
        'testcase.status.active',
        1,
        false,
        'testcase.ts.tswf.2.1~Verify Code Membership~0',
        'specification.ts.tswf.2',
        'SYSTEM_USER',
        'SYSTEM_USER',
        Now(),
        Now(),
        0,
        'testcase.run.environment.eu.testbed','D6A58024X3EFDX4B2DX8C81X461719B08B66')
        ,
       ('testcase.ts.tswf.3.1',
        'Verify Expand ValueSet',
        'Testcase to verify expand value set for specification TSWF3 of the terminology service',
        'testcase.status.active',
        1,
        false,
        'testcase.ts.tswf.3.1~Verify Expand ValueSet~0',
        'specification.ts.tswf.3',
        'SYSTEM_USER',
        'SYSTEM_USER',
        Now(),
        Now(),
        0,
        'testcase.run.environment.eu.testbed','D6A58024X3EFDX4B2DX8C81X461719B08B66'),
       ('testcase.ts.tswf.4.1',
        'Verify conceptMap',
        'Testcase to verify conceptMap for specification TSWF4.',
        'testcase.status.active',
        1,
        false,
        'testcase.ts.tswf.4.1~Verify conceptMap~0',
        'specification.ts.tswf.4',
        'SYSTEM_USER',
        'SYSTEM_USER',
        Now(),
        Now(),
        0,
        'testcase.run.environment.eu.testbed','D6A58024X3EFDX4B2DX8C81X461719B08B66'),
       ('testcase.ts.tswf.5.1',
        'Query Code Systems',
        'Testcase to verify Query Code Systems for specification TSWF5 of the terminology service',
        'testcase.status.active',
        1,
        false,
        'testcase.ts.tswf.5.1~Query Code Systems~0',
        'specification.ts.tswf.5',
        'SYSTEM_USER',
        'SYSTEM_USER',
        Now(),
        Now(),
        0,
        'testcase.run.environment.eu.testbed','D6A58024X3EFDX4B2DX8C81X461719B08B66'),
       ('testcase.ts.tswf.6.1',
        'Query Value Set',
        'Testcase to verify Query Value Set for specification TSWF6 of the terminology service',
        'testcase.status.active',
        1,
        false,
        'testcase.ts.tswf.6.1~Query Value Set~0',
        'specification.ts.tswf.6',
        'SYSTEM_USER',
        'SYSTEM_USER',
        Now(),
        Now(),
        0,
        'testcase.run.environment.eu.testbed','D6A58024X3EFDX4B2DX8C81X461719B08B66'),
       ('testcase.ts.tswf.7.1',
        'Verify Lookup-Code',
        'Testcase to verify lookup-code for specification TSWF7 of the terminology service',
        'testcase.status.active',
        1,
        false,
        'testcase.ts.tswf.7.1~Verify Lookup-Code~0',
        'specification.ts.tswf.7',
        'SYSTEM_USER',
        'SYSTEM_USER',
        Now(),
        Now(),
        0,
        'testcase.run.environment.eu.testbed','D6A58024X3EFDX4B2DX8C81X461719B08B66'),
       ('testcase.ts.tswf.8.1',
        'Verify translate function use',
        'Testcase to verify translate function use for specification TSWF8 of the terminology service',
        'testcase.status.active',
        1,
        false,
        'testcase.ts.tswf.8.1~Verify translate function use~0',
        'specification.ts.tswf.8',
        'SYSTEM_USER',
        'SYSTEM_USER',
        Now(),
        Now(),
        0,
        'testcase.run.environment.eu.testbed','D6A58024X3EFDX4B2DX8C81X461719B08B66');
