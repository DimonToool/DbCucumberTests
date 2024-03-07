Feature: refresh_sa_cdm_periods

  Background: Set up data
    #  Select current day
    When Query "select TO_CHAR(SYSDATE, 'YYYY-MM-DD') as cur_date from dual"
      |cur_date   |
      |>>V_SYSDATE|
#  Select sysdate - 2
    And Query "select TO_CHAR(to_date(sysdate)-2, 'YYYY-MM-DD') as cur_date1 from dual"
      |cur_date1   |
      |>>P_SYSDATE|
#  Select sysdate - 1
    And Query "select TO_CHAR(to_date(sysdate)-1, 'YYYY-MM-DD') as cur_date2 from dual"
      |cur_date2  |
      |>>L_SYSDATE|

  Scenario: Fill, update and execute
#  Insert data in Service_Account
    When Insert into table "service_account"
      |SA_ID|SERVICE_ACCOUNT_ID|PROVISIONING_SYSTEM_ID|ENTERPRISE_ACCOUNT_ID|
      |RAWTOHEX('0x57')    |1057               |'rc'                     |157                  |
#  Select SA_ID
    And Query "select distinct SA_ID from service_account where SA_ID = RAWTOHEX('0x57')"
      |SA_ID  |
      |>>SA_ID|
#  Check Service Account
    And Query "select SERVICE_ACCOUNT_ID,PROVISIONING_SYSTEM_ID,ENTERPRISE_ACCOUNT_ID from service_account where SA_ID = RAWTOHEX('0x57')"
      |SERVICE_ACCOUNT_ID|PROVISIONING_SYSTEM_ID|ENTERPRISE_ACCOUNT_ID|
      |1057              |'rc'                     |157                  |
#  Insert sa_users (like vall_users)
    And Insert into table "sa_users"
      |USERID|PAYSYSTEMID|PRIVILEGEID|
      |157   |24         |1          |
#  Insert Data in cdm_periods_legacy (first record)
    And Insert into table "cdm_periods_legacy"
      |SA_ID|PERIOD_FIRST_DAY|PERIOD_LAST_DAY|CONTRACT_START_DATE|CONTRACT_END_DATE|CONTRACT_DURATION_UNIT|CONTRACT_DURATION_VALUE|TS_INSERTED|TS_UPDATED|
      |RAWTOHEX('0x57')   |date'{<<P_SYSDATE}'     |date'9999-12-31'     |date'2023-01-15'         |date'2023-12-15'       |2                     |12                    |date'{<<P_SYSDATE}' |NULL       |
#  Execute procedures
    And Execute "CONTRACT.REFRESH_SA_CDM_PERIODS()" procedure
#  Check result sa_cdm_periods (legacy)
    Then Query "select to_char(sa.SERVICE_ACCOUNT_ID) AS SERVICE_ACCOUNT_ID, PERIOD_FIRST_DAY, PERIOD_LAST_DAY,PERIOD_TYPE_ID, PERIOD_SRC_ID, QUOTE_ID, CONTRACT_START_DATE, CONTRACT_END_DATE, CONTRACT_DURATION_UNIT, CONTRACT_DURATION_VALUE, TS_INSERTED, TS_UPDATED from sa_cdm_periods s inner join service_account sa on s.sa_id = sa.sa_id where sa.SERVICE_ACCOUNT_ID = '1057'"
      |SERVICE_ACCOUNT_ID|PERIOD_FIRST_DAY       |PERIOD_LAST_DAY        |PERIOD_TYPE_ID|PERIOD_SRC_ID|QUOTE_ID|CONTRACT_START_DATE|CONTRACT_END_DATE|CONTRACT_DURATION_UNIT|CONTRACT_DURATION_VALUE|TS_INSERTED|TS_UPDATED|
      |1057              |2024-02-26  |9999-12-31  |1             |3            |null    |2023-01-15|2023-12-15|2                     |12                     |<<P_SYSDATE|NULL       |
#  Update sa_cdm_periods (legacy)
    When Update table "sa_cdm_periods"
      |SA_ID|PERIOD_FIRST_DAY=|PERIOD_LAST_DAY|PERIOD_TYPE_ID|PERIOD_SRC_ID|QUOTE_ID|CONTRACT_START_DATE|CONTRACT_END_DATE|CONTRACT_DURATION_UNIT|CONTRACT_DURATION_VALUE|
      |RAWTOHEX('0x57')   |date'2024-02-24'|date'9999-12-31'     |1             |3            |null    |date'2023-01-15'|date'2023-12-15'|2                     |12                     |
#  Check sa_cdm_periods result
    Then Query "select to_char(sa.SERVICE_ACCOUNT_ID) AS SERVICE_ACCOUNT_ID, PERIOD_FIRST_DAY, PERIOD_LAST_DAY,PERIOD_TYPE_ID, PERIOD_SRC_ID, QUOTE_ID, CONTRACT_START_DATE, CONTRACT_END_DATE, CONTRACT_DURATION_UNIT, CONTRACT_DURATION_VALUE, TS_INSERTED, TS_UPDATED from sa_cdm_periods s inner join service_account sa on s.sa_id = sa.sa_id where sa.SERVICE_ACCOUNT_ID = '1057'"
      |SERVICE_ACCOUNT_ID|PERIOD_FIRST_DAY       |PERIOD_LAST_DAY        |PERIOD_TYPE_ID|PERIOD_SRC_ID|QUOTE_ID|CONTRACT_START_DATE|CONTRACT_END_DATE|CONTRACT_DURATION_UNIT|CONTRACT_DURATION_VALUE|
      |1057              |2024-02-24  |9999-12-31  |1             |3            |null    |2023-01-15|2023-12-15|2                     |12                     |
#  Update Data in cdm_periods_legacy (first record)
    When Update table "cdm_periods_legacy"
      |SA_ID|PERIOD_FIRST_DAY|PERIOD_LAST_DAY=|CONTRACT_START_DATE=|CONTRACT_END_DATE=|CONTRACT_DURATION_UNIT|CONTRACT_DURATION_VALUE=|TS_INSERTED|TS_UPDATED=|
      |RAWTOHEX('0x57')   |date'{<<P_SYSDATE}'     |date'{<<L_SYSDATE}'     |date'2023-01-15'          |date'2023-12-15'        |2                     |12                    |date'{<<P_SYSDATE}' |date'{<<L_SYSDATE}'|
#  Insert Second record in cdm_periods_legacy (second record)
    And Insert into table "cdm_periods_legacy"
      |SA_ID|PERIOD_FIRST_DAY|PERIOD_LAST_DAY|CONTRACT_START_DATE|CONTRACT_END_DATE|CONTRACT_DURATION_UNIT|CONTRACT_DURATION_VALUE|TS_INSERTED|TS_UPDATED|
      |RAWTOHEX('0x57')   |date'{<<V_SYSDATE}'     |date'9999-12-31'     |date'2023-01-15'         |date'2025-12-15'       |2                     |24                    |date'{<<V_SYSDATE}' |NULL       |
#  Execute procedures
    And Execute "CONTRACT.REFRESH_SA_CDM_PERIODS()" procedure
#  Check sa_cdm_periods
    Then Query "select to_char(sa.SERVICE_ACCOUNT_ID) AS SERVICE_ACCOUNT_ID, PERIOD_FIRST_DAY, PERIOD_LAST_DAY,PERIOD_TYPE_ID, PERIOD_SRC_ID, QUOTE_ID, CONTRACT_START_DATE, CONTRACT_END_DATE, CONTRACT_DURATION_UNIT, CONTRACT_DURATION_VALUE from sa_cdm_periods s inner join service_account sa on s.sa_id = sa.sa_id where sa.SERVICE_ACCOUNT_ID = '1057'"
      |SERVICE_ACCOUNT_ID|PERIOD_FIRST_DAY       |PERIOD_LAST_DAY        |PERIOD_TYPE_ID|PERIOD_SRC_ID|QUOTE_ID|CONTRACT_START_DATE|CONTRACT_END_DATE|CONTRACT_DURATION_UNIT|CONTRACT_DURATION_VALUE|
      |1057              |<<P_SYSDATE  |9999-12-31  |1             |3            |null    |2023-01-15|2023-12-15|2                     |24                     |
#      |1057              |2024-02-26  |9999-12-31  |1             |3            |null    |2023-01-15|2025-12-15|2                     |24                     |
#  Clean Data
    Then Execute DDL "delete from sa_cdm_periods where SA_ID = RAWTOHEX('57')"
    Then Execute DDL "delete from cdm_periods_legacy where SA_ID = RAWTOHEX('57')"
    Then Execute DDL "delete from sa_users where USERID = 157"
    Then Execute DDL "delete from service_account where SA_ID = RAWTOHEX('57')"


#!2 Select current day
#|Query| select to_date(sysdate) as cur_date from dual|
#|cur_date?|
#|>> V_SYSDATE|
#
#!2 Select sysdate - 2
#|Query| select to_date(sysdate)-2 as cur_date1 from dual|
#|cur_date1?|
#|>> P_SYSDATE|
#
#
#!2 Select sysdate - 1
#|Query| select to_date(sysdate) - 1 as cur_date2 from dual|
#|cur_date2?|
#|>> L_SYSDATE|
#
#
#
#!3 Insert data in Service_Account
#!|Insert| service_account|
#|SA_ID|SERVICE_ACCOUNT_ID|PROVISIONING_SYSTEM_ID|ENTERPRISE_ACCOUNT_ID|
#|5|105|rc|15|
#
#!2 Select SA_ID
#!|Query|select distinct SA_ID from service_account|
#|SA_ID?|
#|>> SA_ID|
#
#
#!3 Check Service Account
#!|Query|select SERVICE_ACCOUNT_ID,PROVISIONING_SYSTEM_ID,ENTERPRISE_ACCOUNT_ID from service_account|
#|SERVICE_ACCOUNT_ID|PROVISIONING_SYSTEM_ID|ENTERPRISE_ACCOUNT_ID|
#|105|rc|15|
#
#
#!3 Insert sa_users (like vall_users)
#!|Insert|sa_users|
#|USERID|PAYSYSTEMID|PRIVILEGEID|
#|15|24|1|
#
#
#!3 Insert Data in cdm_periods_legacy (first record)
#!|Insert|cdm_periods_legacy|
#|SA_ID|PERIOD_FIRST_DAY|PERIOD_LAST_DAY|CONTRACT_START_DATE|CONTRACT_END_DATE|CONTRACT_DURATION_UNIT|CONTRACT_DURATION_VALUE|TS_INSERTED|TS_UPDATED|
#|5|<< P_SYSDATE|9999-12-31|2023-01-15|2023-12-15|2|12| << P_SYSDATE |NULL|
#
#
#!3 Execute procedures
#!|Execute procedure|CONTRACT.REFRESH_SA_CDM_PERIODS|
#
#!2 Check result sa_cdm_periods (legacy)
#|Query|select to_char(sa.SERVICE_ACCOUNT_ID) AS SERVICE_ACCOUNT_ID, PERIOD_FIRST_DAY, PERIOD_LAST_DAY,PERIOD_TYPE_ID, PERIOD_SRC_ID, QUOTE_ID, CONTRACT_START_DATE, CONTRACT_END_DATE, CONTRACT_DURATION_UNIT, CONTRACT_DURATION_VALUE, TS_INSERTED, TS_UPDATED from sa_cdm_periods s inner join service_account sa on s.sa_id = sa.sa_id|
#| SERVICE_ACCOUNT_ID | PERIOD_FIRST_DAY | PERIOD_LAST_DAY | PERIOD_TYPE_ID | PERIOD_SRC_ID | QUOTE_ID | CONTRACT_START_DATE | CONTRACT_END_DATE | CONTRACT_DURATION_UNIT | CONTRACT_DURATION_VALUE |
#|105|2024-02-26 00:00:00.0|9999-12-31 00:00:00.0| 1 | 3	| null	             | 2023-01-15 00:00:00.0 |	2023-12-15 00:00:00.0 |	2	 | 12	|
#
#!3 Update sa_cdm_periods (legacy)
#|Update|sa_cdm_periods|
#| SA_ID | PERIOD_FIRST_DAY= | PERIOD_LAST_DAY | PERIOD_TYPE_ID | PERIOD_SRC_ID | QUOTE_ID | CONTRACT_START_DATE | CONTRACT_END_DATE | CONTRACT_DURATION_UNIT | CONTRACT_DURATION_VALUE |
#|<< SA_ID|2024-02-24 00:00:00.0|9999-12-31 00:00:00.0| 1 | 3	| null	             | 2023-01-15 00:00:00.0 |	2023-12-15 00:00:00.0 |	2	 | 12	|
#
#!2 Check sa_cdm_periods result
#!|Query|select to_char(sa.SERVICE_ACCOUNT_ID) AS SERVICE_ACCOUNT_ID, PERIOD_FIRST_DAY, PERIOD_LAST_DAY,PERIOD_TYPE_ID, PERIOD_SRC_ID, QUOTE_ID, CONTRACT_START_DATE, CONTRACT_END_DATE, CONTRACT_DURATION_UNIT, CONTRACT_DURATION_VALUE, TS_INSERTED, TS_UPDATED from sa_cdm_periods s inner join service_account sa on s.sa_id = sa.sa_id|
#| SERVICE_ACCOUNT_ID | PERIOD_FIRST_DAY | PERIOD_LAST_DAY | PERIOD_TYPE_ID | PERIOD_SRC_ID | QUOTE_ID | CONTRACT_START_DATE | CONTRACT_END_DATE | CONTRACT_DURATION_UNIT | CONTRACT_DURATION_VALUE |
#|105|2024-02-24 00:00:00.0|9999-12-31 00:00:00.0| 1 | 3	| null	             | 2023-01-15 00:00:00.0 |	2023-12-15 00:00:00.0 |	2	 | 12	|
#
#
#!3 Update Data in cdm_periods_legacy (first record)
#!|Update|cdm_periods_legacy|
#|SA_ID|PERIOD_FIRST_DAY|PERIOD_LAST_DAY=|CONTRACT_START_DATE=|CONTRACT_END_DATE=|CONTRACT_DURATION_UNIT|CONTRACT_DURATION_VALUE=|TS_INSERTED|TS_UPDATED=|
#|<< SA_ID|<< P_SYSDATE|<< L_SYSDATE|2023-01-15|2023-12-15|2|12| << P_SYSDATE |<< L_SYSDATE|
#
#!3 Insert Second record in cdm_periods_legacy (second record)
#!|Insert|cdm_periods_legacy|
#|SA_ID|PERIOD_FIRST_DAY|PERIOD_LAST_DAY|CONTRACT_START_DATE|CONTRACT_END_DATE|CONTRACT_DURATION_UNIT|CONTRACT_DURATION_VALUE|TS_INSERTED|TS_UPDATED|
#|5|<< V_SYSDATE|9999-12-31|2023-01-15|2025-12-15|2|24| << V_SYSDATE | null |
#
#!3 Execute procedures
#!|Execute procedure|CONTRACT.REFRESH_SA_CDM_PERIODS|
#
#!|Inspect query|select to_char(sa.SERVICE_ACCOUNT_ID) as SERVICE_ACCOUNT_ID, PERIOD_FIRST_DAY, PERIOD_LAST_DAY,PERIOD_TYPE_ID,PERIOD_SRC_ID,QUOTE_ID,CONTRACT_START_DATE,CONTRACT_END_DATE,CONTRACT_DURATION_UNIT,CONTRACT_DURATION_VALUE,TS_INSERTED,TS_UPDATED from sa_cdm_periods s inner join service_account sa on s.sa_id = sa.sa_id|
#
#!2 Check sa_cdm_periods
#!|Query|select to_char(sa.SERVICE_ACCOUNT_ID) AS SERVICE_ACCOUNT_ID, PERIOD_FIRST_DAY, PERIOD_LAST_DAY,PERIOD_TYPE_ID, PERIOD_SRC_ID, QUOTE_ID, CONTRACT_START_DATE, CONTRACT_END_DATE, CONTRACT_DURATION_UNIT, CONTRACT_DURATION_VALUE, TS_INSERTED, TS_UPDATED from sa_cdm_periods s inner join service_account sa on s.sa_id = sa.sa_id|
#| SERVICE_ACCOUNT_ID | PERIOD_FIRST_DAY | PERIOD_LAST_DAY | PERIOD_TYPE_ID | PERIOD_SRC_ID | QUOTE_ID | CONTRACT_START_DATE | CONTRACT_END_DATE | CONTRACT_DURATION_UNIT | CONTRACT_DURATION_VALUE |
#|105|2024-02-24 00:00:00.0|2024-02-25 00:00:00.0| 1 | 3	| null	             | 2023-01-15 00:00:00.0 |	2023-12-15 00:00:00.0 |	2 | 12 |
#|105|2024-02-26 00:00:00.0|9999-12-31 00:00:00.0| 1 | 3	| null	             | 2023-01-15 00:00:00.0 |	2025-12-15 00:00:00.0 |	2 | 24 |
#
#
#!2 Clean Data
#!|Execute Ddl|truncate table cdm_periods_legacy|
#!|Execute Ddl|truncate table sa_cdm_periods|
#!|Execute Ddl|truncate table service_account|
#!|Execute Ddl|truncate table sa_users|