Feature: VSA_CDM_PERIODS

  Scenario: Fill in VSA_CDM_PERIODS from all sources
    When Insert into table "SERVICE_ACCOUNT"
      | SA_ID                                                  | SERVICE_ACCOUNT_ID | PROVISIONING_SYSTEM_ID | ENTERPRISE_ACCOUNT_ID |
      | RAWTOHEX('90B885DD87961D50DDE4B32CA9034CB597570A78')   | 79                 | 'rc'                   | 79                    |

    Then Query "select * from service_account where SA_ID = RAWTOHEX('90B885DD87961D50DDE4B32CA9034CB597570A78')"
      | SA_ID                                    | SERVICE_ACCOUNT_ID | PROVISIONING_SYSTEM_ID | ENTERPRISE_ACCOUNT_ID |
      | 90B885DD87961D50DDE4B32CA9034CB597570A78 | 79                 | rc                     | 79                    |

    When Execute "CONTRACT.REFRESH_SA_CDM_PERIODS" procedure
    Then Query "select * from sa_service_account where SA_ID = RAWTOHEX('90B885DD87961D50DDE4B32CA9034CB597570A78')"
      | SA_ID                                    | SERVICE_ACCOUNT_ID | PROVISIONING_SYSTEM_ID | ENTERPRISE_ACCOUNT_ID |
      | 90B885DD87961D50DDE4B32CA9034CB597570A78 | 79                 | rc                     | 79                    |

    When Execute DDL "delete from service_account where SA_ID = RAWTOHEX('90B885DD87961D50DDE4B32CA9034CB597570A78')"
    Then Query returns nothing "select * from service_account where SA_ID = RAWTOHEX('90B885DD87961D50DDE4B32CA9034CB597570A78')"

    When Update table "cdm_periods_legacy"
      |SA_ID|PERIOD_FIRST_DAY|=PERIOD_LAST_DAY|=CONTRACT_START_DATE|=CONTRACT_END_DATE|CONTRACT_DURATION_UNIT|=CONTRACT_DURATION_VALUE|TS_INSERTED|=TS_UPDATED|
      |<< SA_ID|<< P_SYSDATE|<< L_SYSDATE|2023-01-15|2023-12-15|2|12| << P_SYSDATE |<< L_SYSDATE|