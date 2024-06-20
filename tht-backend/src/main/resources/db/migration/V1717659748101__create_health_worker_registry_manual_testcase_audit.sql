INSERT INTO specification_aud (
    id,
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
    version,
    rev,
    revtype
)
VALUES ('specification.hwr.hwrf.1',
        'HWRF-1',
        'HWRF-1 Specification of the Health Worker Repository Component (https://guides.ohie.org/arch-spec/openhie-component-specifications-1/openhie-health-worker-registry-hwr)',
        'specification.status.active',
        5,
        true,
        true,
        'component.health.worker.registry',
        'SYSTEM_USER',
        'SYSTEM_USER',
        Now(),
        Now(),
        0,
        1,
        0),
       ('specification.hwr.hwrf.2',
        'HWRF-2',
        'HWRF-2 Specification of the Health Worker Repository Component (https://guides.ohie.org/arch-spec/openhie-component-specifications-1/openhie-health-worker-registry-hwr)',
        'specification.status.active',
        6,
        true,
        true,
        'component.health.worker.registry',
        'SYSTEM_USER',
        'SYSTEM_USER',
        Now(),
        Now(),
        0,
        1,
        0),
       ('specification.hwr.hwrf.3',
        'HWRF-3',
        'HWRF-3 Specification of the Health Worker Repository Component (https://guides.ohie.org/arch-spec/introduction/care-services-discovery/query-health-worker-and-or-facility-records-workflow)',
        'specification.status.active',
        7,
        true,
        true,
        'component.health.worker.registry',
        'SYSTEM_USER',
        'SYSTEM_USER',
        Now(),
        Now(),
        0,
        1,
        0),
       ('specification.hwr.hwrf.4',
        'HWRF-4',
        'HWRF-4 Specification of the Health Worker Repository Component (https://guides.ohie.org/arch-spec/introduction/care-services-discovery/query-health-worker-and-or-facility-records-workflow)',
        'specification.status.active',
        8,
        true,
        false,
        'component.health.worker.registry',
        'SYSTEM_USER',
        'SYSTEM_USER',
        Now(),
        Now(),
        0,
        1,
        0),
       ('specification.hwr.hwrf.5',
        'HWRF-5',
        'HWRF-5 Specification of the Health Worker Repository Component (https://guides.ohie.org/arch-spec/introduction/care-services-discovery/query-health-worker-and-or-facility-records-workflow)',
        'specification.status.active',
        9,
        true,
        false,
        'component.health.worker.registry',
        'SYSTEM_USER',
        'SYSTEM_USER',
        Now(),
        Now(),
        0,
        1,
        0),
       ('specification.hwr.hwrf.7',
        'HWRF-7',
        'HWRF-7 Specification of the Health Worker Repository Component (https://guides.ohie.org/arch-spec/introduction/care-services-discovery/query-health-worker-and-or-facility-records-workflow)',
        'specification.status.active',
        11,
        true,
        true,
        'component.health.worker.registry',
        'SYSTEM_USER',
        'SYSTEM_USER',
        Now(),
        Now(),
        0,
        1,
        0);

INSERT INTO testcase_aud (
    id,
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
    version,
    rev,
    revtype
)
VALUES ('testcase.hwr.hwrf.1.1',
        'Does the system support querying source data systems for updates to health worker information, such as update on qualifications or contact details of health care worker?',
        'Instructions to be added',
        'testcase.status.active',
        1,
        true,
        null,
        'SINGLE_SELECT',
        'The system should support the ability to query source data systems for updates (qualifications, contact information change of health worker) to meet the specification',
        'specification.hwr.hwrf.1',
        'SYSTEM_USER',
        'SYSTEM_USER',
        Now(),
        Now(),
        0,
        1,
        0),
       ('testcase.hwr.hwrf.2.1',
        'Does the system support retaining updates received from source data systems, such as storing and updating the latest contact information of healthcare workers?',
        'Instructions to be added',
        'testcase.status.active',
        1,
        true,
        null,
        'SINGLE_SELECT',
        'The system should support the ability to retain received updates from source data systems to meet this specification.',
        'specification.hwr.hwrf.1',
        'SYSTEM_USER',
        'SYSTEM_USER',
        Now(),
        Now(),
        0,
        1,
        0),
       ('testcase.hwr.hwrf.3.1',
        'As a user, does the system provide the capability to query the health worker data stored in its database? For instance, if you want to view information about healthcare worker''s qualifications or contact details, can the system provide you with the information?',
        'Instructions to be added',
        'testcase.status.active',
        1,
        true,
        null,
        'SINGLE_SELECT',
        'The system should support the ability to respond to queries on health worker data to meet this specification.',
        'specification.hwr.hwrf.3',
        'SYSTEM_USER',
        'SYSTEM_USER',
        Now(),
        Now(),
        0,
        1,
        0),
       ('testcase.hwr.hwrf.4.1',
        'Can the system send information to other data repositories, like an InterLinked Registry, to ensure they have the most updated and accurate data?',
        'Instructions to be added',
        'testcase.status.active',
        1,
        true,
        null,
        'SINGLE_SELECT',
        'The system should be able to send updates to other data repositories (such as an InterLinked Registry) to meet this specification.',
        'specification.hwr.hwrf.4',
        'SYSTEM_USER',
        'SYSTEM_USER',
        Now(),
        Now(),
        0,
        1,
        0),
       ('testcase.hwr.hwrf.5.1',
        'Does the system support keeping a history of health worker data when it is updated, ensuring old data isn''t overwritten and previous versions are recorded?',
        'Instructions to be added',
        'testcase.status.active',
        1,
        true,
        null,
        'SINGLE_SELECT',
        'The system should support the ability to maintain version history of health worker data to meet this specification.',
        'specification.hwr.hwrf.5',
        'SYSTEM_USER',
        'SYSTEM_USER',
        Now(),
        Now(),
        0,
        1,
        0),
       ('testcase.hwr.hwrf.7.1',
        'Does the system have flexible standards-based APIs, based on CSD or mCSD?',
        'Instructions to be added',
        'testcase.status.active',
        1,
        true,
        null,
        'SINGLE_SELECT',
        'The system should have flexible standards-based APIs, based on CSD or mCSD to meet this specification.',
        'specification.hwr.hwrf.7',
        'SYSTEM_USER',
        'SYSTEM_USER',
        Now(),
        Now(),
        0,
        1,
        0);

INSERT INTO testcase_option_aud (
    id,
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
    version,
    rev,
    revtype
)
VALUES ('testcase.hwr.hwrf.1.1.option.1',
        'Yes, the system can query for updates to health worker data.',
        'Criteria to be added',
        'testcase.option.status.active',
        1,
        true,
        'testcase.hwr.hwrf.1.1',
        'SYSTEM_USER',
        'SYSTEM_USER',
        Now(),
        Now(),
        0,
        1,
        0),
       ('testcase.hwr.hwrf.1.1.option.2',
        'No, the system cannot query for updates to health worker data.',
        'Criteria to be added',
        'testcase.option.status.active',
        2,
        false,
        'testcase.hwr.hwrf.1.1',
        'SYSTEM_USER',
        'SYSTEM_USER',
        Now(),
        Now(),
        0,
        1,
        0),
       ('testcase.hwr.hwrf.2.1.option.1',
        'Yes, the system can store and update received information.',
        'Criteria to be added',
        'testcase.option.status.active',
        1,
        true,
        'testcase.hwr.hwrf.2.1',
        'SYSTEM_USER',
        'SYSTEM_USER',
        Now(),
        Now(),
        0,
        1,
        0),
       ('testcase.hwr.hwrf.2.1.option.2',
        'No, the system cannot store and update received information.',
        'Criteria to be added',
        'testcase.option.status.active',
        2,
        false,
        'testcase.hwr.hwrf.2.1',
        'SYSTEM_USER',
        'SYSTEM_USER',
        Now(),
        Now(),
        0,
        1,
        0),
       ('testcase.hwr.hwrf.3.1.option.1',
        'Yes, the system provides the ability to query the health worker data to get the required details.',
        'Criteria to be added',
        'testcase.option.status.active',
        1,
        true,
        'testcase.hwr.hwrf.3.1',
        'SYSTEM_USER',
        'SYSTEM_USER',
        Now(),
        Now(),
        0,
        1,
        0),
       ('testcase.hwr.hwrf.3.1.option.2',
        'No, the system does not have the capability to query the health worker data.',
        'Criteria to be added',
        'testcase.option.status.active',
        2,
        false,
        'testcase.hwr.hwrf.3.1',
        'SYSTEM_USER',
        'SYSTEM_USER',
        Now(),
        Now(),
        0,
        1,
        0),
       ('testcase.hwr.hwrf.4.1.option.1',
        'Yes, the system can send information to other data repositories.',
        'Criteria to be added',
        'testcase.option.status.active',
        1,
        true,
        'testcase.hwr.hwrf.4.1',
        'SYSTEM_USER',
        'SYSTEM_USER',
        Now(),
        Now(),
        0,
        1,
        0),
       ('testcase.hwr.hwrf.4.1.option.2',
        'No, the system cannot send information to other data repositories.',
        'Criteria to be added',
        'testcase.option.status.active',
        2,
        false,
        'testcase.hwr.hwrf.4.1',
        'SYSTEM_USER',
        'SYSTEM_USER',
        Now(),
        Now(),
        0,
        1,
        0),
       ('testcase.hwr.hwrf.5.1.option.1',
        'Yes, the system maintains a history of updated health worker data.',
        'Criteria to be added',
        'testcase.option.status.active',
        1,
        true,
        'testcase.hwr.hwrf.5.1',
        'SYSTEM_USER',
        'SYSTEM_USER',
        Now(),
        Now(),
        0,
        1,
        0),
       ('testcase.hwr.hwrf.5.1.option.2',
        'No, the system does not maintain a history of updated health worker data.',
        'Criteria to be added',
        'testcase.option.status.active',
        2,
        false,
        'testcase.hwr.hwrf.5.1',
        'SYSTEM_USER',
        'SYSTEM_USER',
        Now(),
        Now(),
        0,
        1,
        0),
       ('testcase.hwr.hwrf.7.1.option.1',
        'Yes, the system has flexible standards-based APIs based on CSD or mCSD.',
        'Criteria to be added',
        'testcase.option.status.active',
        1,
        true,
        'testcase.hwr.hwrf.7.1',
        'SYSTEM_USER',
        'SYSTEM_USER',
        Now(),
        Now(),
        0,
        1,
        0),
       ('testcase.hwr.hwrf.7.1.option.2',
        'No, the system does not have flexible standards-based APIs based on CSD or mCSD.',
        'Criteria to be added',
        'testcase.option.status.active',
        2,
        false,
        'testcase.hwr.hwrf.7.1',
        'SYSTEM_USER',
        'SYSTEM_USER',
        Now(),
        Now(),
        0,
        1,
        0);
