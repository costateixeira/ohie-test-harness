

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
VALUES ('component.non.functional.requirements',
        'Non-Functional Requirements (NFR)',
        'Non-Functional Requirements Component (https://guides.ohie.org/arch-spec/openhie-component-specifications-1/non-functional-requirements)',
        'component.status.active',
        11,
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
VALUES ('specification.nfr.nfrf.1',
        'NFRF-1',
        'NFRF-1 Specification of the Non-Functional Requirements Component (https://guides.ohie.org/arch-spec/openhie-component-specifications-1/non-functional-requirements)',
        'specification.status.active',
        1,
        true,
        false,
        'component.non.functional.requirements',
        'SYSTEM_USER',
        'SYSTEM_USER',
        Now(),
        Now(),
        0),
       ('specification.nfr.nfrf.2',
        'NFRF-2',
        'NFRF-2 Specification of the Non-Functional Requirements Component (https://guides.ohie.org/arch-spec/openhie-component-specifications-1/non-functional-requirements)',
        'specification.status.active',
        2,
        true,
        false,
        'component.non.functional.requirements',
        'SYSTEM_USER',
        'SYSTEM_USER',
        Now(),
        Now(),
        0),
       ('specification.nfr.nfrf.3',
        'NFRF-3',
        'NFRF-3 Specification of the Non-Functional Requirements Component (https://guides.ohie.org/arch-spec/openhie-component-specifications-1/non-functional-requirements)',
        'specification.status.active',
        3,
        true,
        false,
        'component.non.functional.requirements',
        'SYSTEM_USER',
        'SYSTEM_USER',
        Now(),
        Now(),
        0),
       ('specification.nfr.nfrf.4',
        'NFRF-4',
        'NFRF-4 Specification of the Non-Functional Requirements Component (https://guides.ohie.org/arch-spec/openhie-component-specifications-1/non-functional-requirements)',
        'specification.status.active',
        4,
        true,
        false,
        'component.non.functional.requirements',
        'SYSTEM_USER',
        'SYSTEM_USER',
        Now(),
        Now(),
        0),
       ('specification.nfr.nfrf.5',
        'NFRF-5',
        'NFRF-5 Specification of the Non-Functional Requirements Component (https://guides.ohie.org/arch-spec/openhie-component-specifications-1/non-functional-requirements)',
        'specification.status.active',
        5,
        true,
        false,
        'component.non.functional.requirements',
        'SYSTEM_USER',
        'SYSTEM_USER',
        Now(),
        Now(),
        0),
       ('specification.nfr.nfrf.7',
        'NFRF-7',
        'NFRF-7 Specification of the Non-Functional Requirements Component (https://guides.ohie.org/arch-spec/openhie-component-specifications-1/non-functional-requirements)',
        'specification.status.active',
        7,
        true,
        false,
        'component.non.functional.requirements',
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
                      bean_name,
                      question_type,
                      failure_message,
                      specification_id,
                      created_by,
                      updated_by,
                      created_at,
                      updated_at,
                      version)
VALUES ('testcase.nfr.nfrf.1.1',
        'Are you able to easily access the data stored within the system?',
        'Instructions to be added',
        'testcase.status.active',
        1,
        true,
        null,
        'SINGLE_SELECT',
        'The technologies should provide standard means of accessing data within the system to meet this specification.',
        'specification.nfr.nfrf.1',
        'SYSTEM_USER',
        'SYSTEM_USER',
        Now(),
        Now(),
        0),
       ('testcase.nfr.nfrf.2.1',
        'Is the OpenHIE reference system well-documented, providing background, design, installation, configuration, and operational documentation to ensure easy understanding, maintenance, and debugging?',
        'Instructions to be added',
        'testcase.status.active',
        1,
        true,
        null,
        'SINGLE_SELECT',
        'The system should be well documented. It should include appropriate background design, installation, configuration, and operational documentation to meet this specification.',
        'specification.nfr.nfrf.2',
        'SYSTEM_USER',
        'SYSTEM_USER',
        Now(),
        Now(),
        0),
       ('testcase.nfr.nfrf.2.2',
        'Does the source code of the OpenHIE reference system have comments, allowing developers to understand the code without needing to refer to external sources?',
        'Instructions to be added',
        'testcase.status.active',
        2,
        true,
        null,
        'SINGLE_SELECT',
        'The system should be well documented. It should include appropriate background design, installation, configuration, and operational documentation to meet this specification.',
        'specification.nfr.nfrf.2',
        'SYSTEM_USER',
        'SYSTEM_USER',
        Now(),
        Now(),
        0),
       ('testcase.nfr.nfrf.2.3',
        'Do configuration files of the OpenHIE reference system have embedded comments explaining different options?',
        'Instructions to be added',
        'testcase.status.active',
        3,
        true,
        null,
        'SINGLE_SELECT',
        'The system should be well documented. It should include appropriate background design, installation, configuration, and operational documentation to meet this specification.',
        'specification.nfr.nfrf.2',
        'SYSTEM_USER',
        'SYSTEM_USER',
        Now(),
        Now(),
        0),
       ('testcase.nfr.nfrf.2.4',
        'Are installation, configuration, and operational activities of the OpenHIE reference system adequately described and available in the GitHub repository?',
        'Instructions to be added',
        'testcase.status.active',
        4,
        true,
        null,
        'SINGLE_SELECT',
        'The system should be well documented. It should include appropriate background design, installation, configuration, and operational documentation to meet this specification.',
        'specification.nfr.nfrf.2',
        'SYSTEM_USER',
        'SYSTEM_USER',
        Now(),
        Now(),
        0),
       ('testcase.nfr.nfrf.3.1',
        'If the system is an open-source tool, does it provide open and easy access to the source code? For instance, is a standard version control system like GitHub used to ensure fast and easy downloading, compiling, and executing of code?',
        'This interface should suggest potential matches, ask your opinion to merge records that matches and can look something like this:<br/> Review potential matches:<br/>Operation 1: John Doe (January 15, 1980):<br/>Record 1:  Address: 123 Main Street, (555) 123-4567<br/>Record 2:  Address: 456 Elm Street, (555) 987-6543',
        'testcase.status.active',
        1,
        true,
        null,
        'SINGLE_SELECT',
        'If the system is an open source tool, the system should have open, easy access to source code via a standard version control system (e.g., GitHub) to meet this specification.',
        'specification.nfr.nfrf.3',
        'SYSTEM_USER',
        'SYSTEM_USER',
        Now(),
        Now(),
        0),
       ('testcase.nfr.nfrf.4.1',
        'Are the technologies used in the system easy to understand both in terms of technologies and database management?',
        'Instructions to be added',
        'testcase.status.active',
        1,
        true,
        null,
        'SINGLE_SELECT',
        'The system should be built using common technology so it is easy to run/configure/debug and any external systems (like database) is easy to use to meet this specification.',
        'specification.nfr.nfrf.4',
        'SYSTEM_USER',
        'SYSTEM_USER',
        Now(),
        Now(),
        0),
       ('testcase.nfr.nfrf.4.2',
        'Is the system built using common technology, making it easy to run, configure, and debug? For example, is it built on popular technologies widely accepted in the development community?',
        'Instructions to be added',
        'testcase.status.active',
        2,
        true,
        null,
        'SINGLE_SELECT',
        'The system should be built using common technology so it is easy to run/configure/debug and any external systems (like database) is easy to use to meet this specification.',
        'specification.nfr.nfrf.4',
        'SYSTEM_USER',
        'SYSTEM_USER',
        Now(),
        Now(),
        0),
       ('testcase.nfr.nfrf.4.3',
        'Are any 3rd party libraries used by the software easy for a typical developer to use?',
        'Instructions to be added',
        'testcase.status.active',
        3,
        true,
        null,
        'SINGLE_SELECT',
        'The system should be built using common technology so it is easy to run/configure/debug and any external systems (like database) is easy to use to meet this specification.',
        'specification.nfr.nfrf.4',
        'SYSTEM_USER',
        'SYSTEM_USER',
        Now(),
        Now(),
        0),
       ('testcase.nfr.nfrf.4.4',
        'Are external software/systems, like the database (e.g., PostgreSQL), easy to use?',
        'Instructions to be added',
        'testcase.status.active',
        4,
        true,
        null,
        'SINGLE_SELECT',
        'The system should be built using common technology so it is easy to run/configure/debug and any external systems (like database) is easy to use to meet this specification.',
        'specification.nfr.nfrf.4',
        'SYSTEM_USER',
        'SYSTEM_USER',
        Now(),
        Now(),
        0),
       ('testcase.nfr.nfrf.4.5',
        'Is it easy to view the contents of the database, for example, by using tools like MySQL Workbench?',
        'Instructions to be added',
        'testcase.status.active',
        5,
        true,
        null,
        'SINGLE_SELECT',
        'The system should be built using common technology so it is easy to run/configure/debug and any external systems (like database) is easy to use to meet this specification.',
        'specification.nfr.nfrf.4',
        'SYSTEM_USER',
        'SYSTEM_USER',
        Now(),
        Now(),
        0),
       ('testcase.nfr.nfrf.5.1',
        'Does the source code of the system include unit tests designed to meet the specific requirements of OpenHIE? These tests should form a framework to ensure the functionality is validated and the system operates as intended.',
        'Instructions to be added',
        'testcase.status.active',
        1,
        true,
        null,
        'SINGLE_SELECT',
        'The source code should include unit tests that are based on the specific requirements of OpenHIE to meet this specification.',
        'specification.nfr.nfrf.5',
        'SYSTEM_USER',
        'SYSTEM_USER',
        Now(),
        Now(),
        0),
       ('testcase.nfr.nfrf.7.1',
        'Is the system designed to work well in places where there might be limited electricity, internet access, and technical knowledge?',
        'Instructions to be added',
        'testcase.status.active',
        1,
        true,
        null,
        'SINGLE_SELECT',
        'The system should take into account the IT infrastructure of low resource settings where electricity, internet and/or technical literacy may be limited to meet this specification.',
        'specification.nfr.nfrf.7',
        'SYSTEM_USER',
        'SYSTEM_USER',
        Now(),
        Now(),
        0);


INSERT INTO testcase_option (id,
                             name,
                             description,
                             state,
                             rank,
                             is_success,
                             testcase_id,
                             created_by,
                             updated_by,
                             created_at,
                             updated_at,
                             version)
VALUES ('testcase.nfr.nfrf.1.1.option.1',
        'Yes, I am able to access the data.',
        'Criteria to be added',
        'testcase.option.status.active',
        1,
        true,
        'testcase.nfr.nfrf.1.1',
        'SYSTEM_USER',
        'SYSTEM_USER',
        Now(),
        Now(),
        0),
       ('testcase.nfr.nfrf.1.1.option.2',
        'No, I am finding it difficult to access the data.',
        'Criteria to be added',
        'testcase.option.status.active',
        2,
        false,
        'testcase.nfr.nfrf.1.1',
        'SYSTEM_USER',
        'SYSTEM_USER',
        Now(),
        Now(),
        0),
       ('testcase.nfr.nfrf.2.1.option.1',
        'Yes, the OpenHIE reference system is well-documented with information on background, design, installation, configuration, and operational aspects.',
        'Criteria to be added',
        'testcase.option.status.active',
        1,
        true,
        'testcase.nfr.nfrf.2.1',
        'SYSTEM_USER',
        'SYSTEM_USER',
        Now(),
        Now(),
        0),
       ('testcase.nfr.nfrf.2.1.option.2',
        'No, the OpenHIE reference system lacks comprehensive documentation, making it challenging to understand, maintain, and debug.',
        'Criteria to be added',
        'testcase.option.status.active',
        2,
        false,
        'testcase.nfr.nfrf.2.1',
        'SYSTEM_USER',
        'SYSTEM_USER',
        Now(),
        Now(),
        0),
       ('testcase.nfr.nfrf.2.2.option.1',
        'Yes, the source code includes comments that aid developers in understanding the code directly.',
        'Criteria to be added',
        'testcase.option.status.active',
        1,
        true,
        'testcase.nfr.nfrf.2.2',
        'SYSTEM_USER',
        'SYSTEM_USER',
        Now(),
        Now(),
        0),
       ('testcase.nfr.nfrf.2.2.option.2',
        'No, the source code lacks comments, making it difficult for developers to comprehend without external references.',
        'Criteria to be added',
        'testcase.option.status.active',
        2,
        false,
        'testcase.nfr.nfrf.2.2',
        'SYSTEM_USER',
        'SYSTEM_USER',
        Now(),
        Now(),
        0),
       ('testcase.nfr.nfrf.2.3.option.1',
        'Yes, configuration files include embedded comments explaining various options for easy understanding.',
        'Criteria to be added',
        'testcase.option.status.active',
        1,
        true,
        'testcase.nfr.nfrf.2.3',
        'SYSTEM_USER',
        'SYSTEM_USER',
        Now(),
        Now(),
        0),
       ('testcase.nfr.nfrf.2.3.option.2',
        'No, configuration files lack embedded comments, making it challenging to comprehend the different options.',
        'Criteria to be added',
        'testcase.option.status.active',
        2,
        false,
        'testcase.nfr.nfrf.2.3',
        'SYSTEM_USER',
        'SYSTEM_USER',
        Now(),
        Now(),
        0),
       ('testcase.nfr.nfrf.2.4.option.1',
        'Yes, the documentation provides clear descriptions of installation, configuration, and operational activities.',
        'Criteria to be added',
        'testcase.option.status.active',
        1,
        true,
        'testcase.nfr.nfrf.2.4',
        'SYSTEM_USER',
        'SYSTEM_USER',
        Now(),
        Now(),
        0),
       ('testcase.nfr.nfrf.2.4.option.2',
        'No, the documentation lacks clear descriptions of installation, configuration, and operational activities.',
        'Criteria to be added',
        'testcase.option.status.active',
        2,
        false,
        'testcase.nfr.nfrf.2.4',
        'SYSTEM_USER',
        'SYSTEM_USER',
        Now(),
        Now(),
        0),
       ('testcase.nfr.nfrf.3.1.option.1',
        'Yes, the system, being open source, provides easy access to the source code through a standard version control system like GitHub.',
        'Criteria to be added',
        'testcase.option.status.active',
        1,
        true,
        'testcase.nfr.nfrf.3.1',
        'SYSTEM_USER',
        'SYSTEM_USER',
        Now(),
        Now(),
        0),
       ('testcase.nfr.nfrf.3.1.option.2',
        'No, the system, despite being open source, does not facilitate easy access to the source code through a standard version control system.',
        'Criteria to be added',
        'testcase.option.status.active',
        2,
        false,
        'testcase.nfr.nfrf.3.1',
        'SYSTEM_USER',
        'SYSTEM_USER',
        Now(),
        Now(),
        0),
       ('testcase.nfr.nfrf.4.1.option.1',
        'Yes, the technologies used in the system are easy to understand in terms of both technologies and database management.',
        'Criteria to be added',
        'testcase.option.status.active',
        1,
        true,
        'testcase.nfr.nfrf.4.1',
        'SYSTEM_USER',
        'SYSTEM_USER',
        Now(),
        Now(),
        0),
       ('testcase.nfr.nfrf.4.1.option.2',
        'No, the technologies used in the system are not straightforward and pose challenges in understanding both technologies and database management.',
        'Criteria to be added',
        'testcase.option.status.active',
        2,
        false,
        'testcase.nfr.nfrf.4.1',
        'SYSTEM_USER',
        'SYSTEM_USER',
        Now(),
        Now(),
        0),
       ('testcase.nfr.nfrf.4.2.option.1',
        'Yes, the system is built using common technology, making it easy to run, configure, and debug.',
        'Criteria to be added',
        'testcase.option.status.active',
        1,
        true,
        'testcase.nfr.nfrf.4.2',
        'SYSTEM_USER',
        'SYSTEM_USER',
        Now(),
        Now(),
        0),
       ('testcase.nfr.nfrf.4.2.option.2',
        'No, the system is not built using common technology, making it difficult to run, configure, and debug.',
        'Criteria to be added',
        'testcase.option.status.active',
        2,
        false,
        'testcase.nfr.nfrf.4.2',
        'SYSTEM_USER',
        'SYSTEM_USER',
        Now(),
        Now(),
        0),
       ('testcase.nfr.nfrf.4.3.option.1',
        'Yes, any 3rd party libraries used by the software are easy for a typical developer to use.',
        'Criteria to be added',
        'testcase.option.status.active',
        1,
        true,
        'testcase.nfr.nfrf.4.3',
        'SYSTEM_USER',
        'SYSTEM_USER',
        Now(),
        Now(),
        0),
       ('testcase.nfr.nfrf.4.3.option.2',
        'No, the 3rd party libraries used by the software are not user-friendly for typical developers.',
        'Criteria to be added',
        'testcase.option.status.active',
        2,
        false,
        'testcase.nfr.nfrf.4.3',
        'SYSTEM_USER',
        'SYSTEM_USER',
        Now(),
        Now(),
        0),
       ('testcase.nfr.nfrf.4.4.option.1',
        'Yes, external software/systems, including the database, PostgreSQL, are user-friendly.',
        'Criteria to be added',
        'testcase.option.status.active',
        1,
        true,
        'testcase.nfr.nfrf.4.4',
        'SYSTEM_USER',
        'SYSTEM_USER',
        Now(),
        Now(),
        0),
       ('testcase.nfr.nfrf.4.4.option.2',
        'No, external software/systems, including the database, MongoDB, pose usability challenges.',
        'Criteria to be added',
        'testcase.option.status.active',
        2,
        false,
        'testcase.nfr.nfrf.4.4',
        'SYSTEM_USER',
        'SYSTEM_USER',
        Now(),
        Now(),
        0),
       ('testcase.nfr.nfrf.4.5.option.1',
        'Yes, viewing the contents of the database using tools like MySQL Workbench is straightforward.',
        'Criteria to be added',
        'testcase.option.status.active',
        1,
        true,
        'testcase.nfr.nfrf.4.5',
        'SYSTEM_USER',
        'SYSTEM_USER',
        Now(),
        Now(),
        0),
       ('testcase.nfr.nfrf.4.5.option.2',
        'No, viewing the contents of the database requires complex processes and tools.',
        'Criteria to be added',
        'testcase.option.status.active',
        2,
        false,
        'testcase.nfr.nfrf.4.5',
        'SYSTEM_USER',
        'SYSTEM_USER',
        Now(),
        Now(),
        0),
       ('testcase.nfr.nfrf.5.1.option.1',
        'Yes, the source code includes unit tests based on OpenHIE requirements.',
        'Criteria to be added',
        'testcase.option.status.active',
        1,
        true,
        'testcase.nfr.nfrf.5.1',
        'SYSTEM_USER',
        'SYSTEM_USER',
        Now(),
        Now(),
        0),
       ('testcase.nfr.nfrf.5.1.option.2',
        'No, the source code lacks unit tests specific to OpenHIE requirements.',
        'Criteria to be added',
        'testcase.option.status.active',
        2,
        false,
        'testcase.nfr.nfrf.5.1',
        'SYSTEM_USER',
        'SYSTEM_USER',
        Now(),
        Now(),
        0),
       ('testcase.nfr.nfrf.7.1.option.1',
        'Yes, the system is made to work in such places.',
        'Criteria to be added',
        'testcase.option.status.active',
        1,
        true,
        'testcase.nfr.nfrf.7.1',
        'SYSTEM_USER',
        'SYSTEM_USER',
        Now(),
        Now(),
        0),
       ('testcase.nfr.nfrf.7.2.option.2',
        'No, the system might not be suitable for areas with these limitations.',
        'Criteria to be added',
        'testcase.option.status.active',
        2,
        false,
        'testcase.nfr.nfrf.7.1',
        'SYSTEM_USER',
        'SYSTEM_USER',
        Now(),
        Now(),
        0);