package com.qianyi.casinocore.util;

public class SqlConst {
    public static String wmSql = "select \n" +
            "u.account , \n" +
            "u.third_proxy , \n" +
            " u.id,\n" +
            " ifnull(main_t.num,0) num,\n" +
            " ifnull(main_t.bet_amount,0) bet_amount ,\n" +
            " ifnull(main_t.validbet,0) validbet ,\n" +
            " ifnull(main_t.win_loss,0) win_loss ,\n" +
            " ifnull(wash_t.wash_amount,0) wash_amount, \n" +
            " ifnull(withdraw_t.service_charge,0) service_charge, \n" +
            " ifnull(pr.amount,0) all_profit_amount, \n" +
            " -(ifnull(main_t.win_loss,0)+ifnull(wash_t.wash_amount,0)+ifnull(ec.water, 0)) avg_benefit, \n" +
            " -(ifnull(main_t.win_loss,0)+ifnull(wash_t.wash_amount,0))-ifnull(pr.amount,0)-ifnull(ec.water,0)+ifnull(withdraw_t.service_charge,0) total_amount,\n" +
            "  ifnull(ec.water, 0) all_water\n" +
            "from user u\n" +
            "left join ( \n" +
            "  select user_id , \n" +
            "  count(1) num, \n" +
            "  sum(bet) bet_amount, \n" +
            "  sum(validbet) validbet , \n" +
            "  sum(win_loss) win_loss  \n" +
            "  from game_record gr \n" +
            "  where bet_time >= {0} and bet_time <= {1}\n" +
            "  group by user_id \n" +
            " ) main_t on u.id = main_t.user_id\n" +
            " left join ( \n" +
            "  select user_id , sum(amount) wash_amount  \n" +
            "  from wash_code_change wcc  \n" +
            "  where create_time >= {0} and create_time <= {1}\n" +
            "  and platform = {5} " +
            "  group by user_id \n" +
            " ) wash_t on u.id = wash_t.user_id\n" +
            " left join ( \n" +
            "  select user_id , sum(ifnull(service_charge,0)) service_charge  \n" +
            "  from withdraw_order wo  \n" +
            "  where update_time >= {0} and update_time <= {1}\n" +
            "  group by user_id \n" +
            " ) withdraw_t on u.id = withdraw_t.user_id\n" +
            " left join ( \n" +
            "  select user_id , sum(amount) amount from share_profit_change spc  \n" +
            "  where create_time >= {0} and create_time <= {1}\n" +
            "  group by user_id  \n" +
            " ) pr on u.id=pr.user_id \n" +
            "  left join (\n" +
            "\tSELECT user_id, SUM(amount) as water FROM extract_points_change \n" +
            "  where create_time >= {0} and create_time <= {1}\n" +
            "  and platform = {5}"+
            "\tgroup by user_id \n" +
            " ) ec on u.id = ec.user_id\n" +
            " where 1=1 {2} \n" +
            "limit {3},{4}\n";

    public static String totalSql = "select \n" +
            "u.account , \n" +
            "u.third_proxy , \n" +
            " u.id,\n" +
            " ifnull(main_t.num,0)+ifnull(goldenf_t.num,0) num,\n" +
            " ifnull(main_t.bet_amount,0)+ifnull(goldenf_t.bet_amount,0) bet_amount ,\n" +
            " ifnull(main_t.validbet,0)+ifnull(goldenf_t.bet_amount,0) validbet ,\n" +
            " ifnull(main_t.win_loss,0)+ifnull(goldenf_t.win_loss,0) win_loss ,\n" +
            " ifnull(wash_t.wash_amount,0) wash_amount, \n" +
            " ifnull(withdraw_t.service_charge,0) service_charge, \n" +
            " ifnull(pr.amount,0) all_profit_amount, \n" +
            " -(ifnull(main_t.win_loss,0)+ifnull(goldenf_t.win_loss,0)+ifnull(wash_t.wash_amount,0)+ifnull(ec.water,0)) avg_benefit, \n" +
            " -(ifnull(main_t.win_loss,0)+ifnull(goldenf_t.win_loss,0)+ifnull(wash_t.wash_amount,0))-ifnull(pr.amount,0) -ifnull(ec.water,0) +ifnull(withdraw_t.service_charge,0) total_amount,\n" +
            " ifnull(ec.water, 0) all_water\n" +
            "from user u\n" +
            "left join ( \n" +
            "  select user_id , \n" +
            "  count(1) num, \n" +
            "  sum(bet) bet_amount, \n" +
            "  sum(validbet) validbet , \n" +
            "  sum(win_loss) win_loss  \n" +
            "  from game_record gr \n" +
            "  where bet_time >= {0} and bet_time <= {1}\n" +
            "  group by user_id \n" +
            " ) main_t on u.id = main_t.user_id\n" +
            " left join ( \n" +
            "  select user_id , \n" +
            "  count(1) num, \n" +
            "  sum(bet_amount) bet_amount, \n" +
            "  sum(win_amount-bet_amount) win_loss  \n" +
            "  from game_record_goldenf grg \n" +
            "  where create_at_str >= {0} and create_at_str <= {1}\n" +
            "  group by user_id \n" +
            " ) goldenf_t on u.id = goldenf_t.user_id\n" +
            " left join ( \n" +
            "  select user_id , sum(amount) wash_amount  \n" +
            "  from wash_code_change wcc  \n" +
            "  where create_time >= {0} and create_time <= {1}\n" +
            "  group by user_id \n" +
            " ) wash_t on u.id = wash_t.user_id\n" +
            " left join ( \n" +
            "  select user_id , sum(ifnull(service_charge,0)) service_charge  \n" +
            "  from withdraw_order wo  \n" +
            "  where update_time >= {0} and update_time <= {1}\n" +
            "  group by user_id \n" +
            " ) withdraw_t on u.id = withdraw_t.user_id\n" +
            " left join ( \n" +
            "  select user_id , sum(amount) amount from share_profit_change spc  \n" +
            "  where create_time >= {0} and create_time <= {1}\n" +
            "  group by user_id  \n" +
            " ) pr on u.id=pr.user_id \n" +
            " left join (\n" +
            "\tSELECT user_id, SUM(amount) as water FROM extract_points_change \n" +
            "\twhere create_time >= {0} and create_time <= {1}\n" +
            "\tgroup by user_id \n" +
            " ) ec on u.id = ec.user_id\n" +
            " where  1=1 {2} \n" +
            "limit {3},{4}";

    public static String pgOrCq9Sql = "select \n" +
            "u.account , \n" +
            "u.third_proxy , \n" +
            " u.id,\n" +
            " ifnull(goldenf_t.num,0) num,\n" +
            " ifnull(goldenf_t.bet_amount,0) bet_amount ,\n" +
            " ifnull(goldenf_t.bet_amount,0) validbet ,\n" +
            " ifnull(goldenf_t.win_loss,0) win_loss ,\n" +
            " ifnull(wash_t.wash_amount,0) wash_amount, \n" +
            " ifnull(withdraw_t.service_charge,0) service_charge, \n" +
            " ifnull(pr.amount,0) all_profit_amount, \n" +
            " -(ifnull(goldenf_t.win_loss,0)+ifnull(wash_t.wash_amount,0)+ifnull(ec.water, 0)) avg_benefit, \n" +
            " -(ifnull(goldenf_t.win_loss,0)+ifnull(wash_t.wash_amount,0))-ifnull(pr.amount,0)-ifnull(ec.water,0)+ifnull(withdraw_t.service_charge,0) total_amount,\n" +
            " ifnull(ec.water, 0) all_water\n" +
            "from user u\n" +
            "left join ( \n" +
            "  select user_id , \n" +
            "  count(1) num, \n" +
            "  sum(bet_amount) bet_amount, \n" +
            "  sum(win_amount-bet_amount) win_loss  \n" +
            "  from game_record_goldenf grg \n" +
            "  where vendor_code = {5}  and \n" +
            "   create_at_str >= {0} and create_at_str <= {1}\n" +
            "  group by user_id \n" +
            " ) goldenf_t on u.id = goldenf_t.user_id\n" +
            " left join ( \n" +
            "  select user_id , sum(amount) wash_amount  \n" +
            "  from wash_code_change wcc  \n" +
            "  where create_time >= {0} and create_time <= {1}\n" +
            "  and platform = {5} "+
            "  group by user_id \n" +
            " ) wash_t on u.id = wash_t.user_id\n" +
            " left join ( \n" +
            "  select user_id , sum(ifnull(service_charge,0)) service_charge  \n" +
            "  from withdraw_order wo  \n" +
            "  where update_time >= {0} and update_time <= {1}\n" +
            "  group by user_id \n" +
            " ) withdraw_t on u.id = withdraw_t.user_id\n" +
            " left join ( \n" +
            "  select user_id , sum(amount) amount from share_profit_change spc  \n" +
            "  where create_time >= {0} and create_time <= {1}\n" +
            "  group by user_id  \n" +
            " ) pr on u.id=pr.user_id \n" +
            "  left join (\n" +
            "\tSELECT user_id, SUM(amount) as water FROM extract_points_change \n" +
            "\twhere create_time >= {0} and create_time <= {1}\n" +
            "  and platform = {5}"+
            "\tgroup by user_id \n" +
            " ) ec on u.id = ec.user_id\n" +
            " where  1=1 {2} \n" +
            "limit {3},{4}";

    public static String seleOneTotal = "select \n" +
            "u.account , \n" +
            "u.third_proxy , \n" +
            " u.id,\n" +
            " ifnull(main_t.num,0)+ifnull(goldenf_t.num,0) num,\n" +
            " ifnull(main_t.bet_amount,0)+ifnull(goldenf_t.bet_amount,0) bet_amount ,\n" +
            " ifnull(main_t.validbet,0)+ifnull(goldenf_t.bet_amount,0) validbet ,\n" +
            " ifnull(main_t.win_loss,0)+ifnull(goldenf_t.win_loss,0) win_loss ,\n" +
            " ifnull(wash_t.wash_amount,0) wash_amount, \n" +
            " ifnull(withdraw_t.service_charge,0) service_charge, \n" +
            " ifnull(pr.amount,0) all_profit_amount, \n" +
            " -(ifnull(main_t.win_loss,0)+ifnull(goldenf_t.win_loss,0)+ifnull(wash_t.wash_amount,0)+ifnull(ec.water,0)) avg_benefit, \n" +
            " -(ifnull(main_t.win_loss,0)+ifnull(goldenf_t.win_loss,0)+ifnull(wash_t.wash_amount,0))-ifnull(pr.amount,0)-ifnull(ec.water,0)+ifnull(withdraw_t.service_charge,0) total_amount,\n" +
            " sum(ifnull(ec.water, 0)) all_water\n" +
            "from user u\n" +
            "left join ( \n" +
            "  select user_id , \n" +
            "  count(1) num, \n" +
            "  sum(bet) bet_amount, \n" +
            "  sum(validbet) validbet , \n" +
            "  sum(win_loss) win_loss  \n" +
            "  from game_record gr \n" +
            "  where bet_time >= {0} and bet_time <= {1}\n" +
            "  group by user_id \n" +
            " ) main_t on u.id = main_t.user_id\n" +
            " left join ( \n" +
            "  select user_id , \n" +
            "  count(1) num, \n" +
            "  sum(bet_amount) bet_amount, \n" +
            "  sum(win_amount-bet_amount) win_loss  \n" +
            "  from game_record_goldenf grg \n" +
            "  where create_at_str >= {0} and create_at_str <= {1}\n" +
            "  group by user_id \n" +
            " ) goldenf_t on u.id = goldenf_t.user_id\n" +
            " left join ( \n" +
            "  select user_id , sum(amount) wash_amount  \n" +
            "  from wash_code_change wcc  \n" +
            "  where create_time >= {0} and create_time <= {1}\n" +
            "  group by user_id \n" +
            " ) wash_t on u.id = wash_t.user_id\n" +
            " left join ( \n" +
            "  select user_id , sum(ifnull(service_charge,0)) service_charge  \n" +
            "  from withdraw_order wo  \n" +
            "  where update_time >= {0} and update_time <= {1}\n" +
            "  group by user_id \n" +
            " ) withdraw_t on u.id = withdraw_t.user_id\n" +
            " left join ( \n" +
            "  select user_id , sum(amount) amount from share_profit_change spc  \n" +
            "  where create_time >= {0} and create_time <= {1}\n" +
            "  group by user_id  \n" +
            " ) pr on u.id=pr.user_id \n" +
            "   left join (\n" +
            "\tSELECT user_id, SUM(amount) as water FROM extract_points_change \n" +
            "\twhere create_time >= {0} and create_time <= {1}\n" +
            "\tgroup by user_id \n" +
            " ) ec on u.id = ec.user_id\n" +
            " where 1=1 and u.id = {2}";


    public static String seleOnePgOrCq9Sql = "select \n" +
            "u.account , \n" +
            "u.third_proxy , \n" +
            " u.id,\n" +
            " ifnull(goldenf_t.num,0) num,\n" +
            " ifnull(goldenf_t.bet_amount,0) bet_amount ,\n" +
            " ifnull(goldenf_t.bet_amount,0) validbet ,\n" +
            " ifnull(goldenf_t.win_loss,0) win_loss ,\n" +
            " ifnull(wash_t.wash_amount,0) wash_amount, \n" +
            " ifnull(withdraw_t.service_charge,0) service_charge, \n" +
            " ifnull(pr.amount,0) all_profit_amount, \n" +
            " -(ifnull(goldenf_t.win_loss,0)+ifnull(wash_t.wash_amount,0)+ifnull(ec.water,0)) avg_benefit, \n" +
            " -(ifnull(goldenf_t.win_loss,0)+ifnull(wash_t.wash_amount,0))-ifnull(pr.amount,0)-ifnull(ec.water,0)+ifnull(withdraw_t.service_charge,0) total_amount,\n" +
            " sum(ifnull(ec.water, 0)) all_water\n" +
            "from user u\n" +
            " left join ( \n" +
            "  select user_id , \n" +
            "  count(1) num, \n" +
            "  sum(bet_amount) bet_amount, \n" +
            "  sum(win_amount-bet_amount) win_loss  \n" +
            "  from game_record_goldenf grg \n" +
            "  where vendor_code = {3} and create_at_str >= {0} and create_at_str <= {1}\n" +
            "  group by user_id \n" +
            " ) goldenf_t on u.id = goldenf_t.user_id\n" +
            " left join ( \n" +
            "  select user_id , sum(amount) wash_amount  \n" +
            "  from wash_code_change wcc  \n" +
            "  where platform = {3} and  create_time >= {0} and create_time <= {1}\n" +
            "  group by user_id \n" +
            " ) wash_t on u.id = wash_t.user_id\n" +
            " left join ( \n" +
            "  select user_id , sum(ifnull(service_charge,0)) service_charge  \n" +
            "  from withdraw_order wo  \n" +
            "  where update_time >= {0} and update_time <= {1}\n" +
            "  group by user_id \n" +
            " ) withdraw_t on u.id = withdraw_t.user_id\n" +
            " left join ( \n" +
            "  select user_id , sum(amount) amount from share_profit_change spc  \n" +
            "  where create_time >= {0} and create_time <= {1}\n" +
            "  group by user_id  \n" +
            " ) pr on u.id=pr.user_id \n" +
            "   left join (\n" +
            "\tSELECT user_id, SUM(amount) as water FROM extract_points_change \n" +
            "\twhere create_time >= {0} and create_time <= {1}\n" +
            " and platform = {3} "+
            "\tgroup by user_id \n" +
            " ) ec on u.id = ec.user_id\n" +
            " where 1=1 and u.id = {2}";

    public static String seleOneWm = "select \n" +
            "u.account , \n" +
            "u.third_proxy , \n" +
            " u.id,\n" +
            " ifnull(main_t.num,0) num,\n" +
            " ifnull(main_t.bet_amount,0) bet_amount ,\n" +
            " ifnull(main_t.validbet,0) validbet ,\n" +
            " ifnull(main_t.win_loss,0) win_loss ,\n" +
            " ifnull(wash_t.wash_amount,0) wash_amount, \n" +
            " ifnull(withdraw_t.service_charge,0) service_charge, \n" +
            " ifnull(pr.amount,0) all_profit_amount, \n" +
            " -(ifnull(main_t.win_loss,0)+ifnull(wash_t.wash_amount,0)+ifnull(ec.water,0)) avg_benefit, \n" +
            " -(ifnull(main_t.win_loss,0)+ifnull(wash_t.wash_amount,0))-ifnull(pr.amount,0)-ifnull(ec.water,0)+ifnull(withdraw_t.service_charge,0) total_amount,\n" +
            " sum(ifnull(ec.water, 0)) all_water\n" +
            "from user u\n" +
            "left join ( \n" +
            "  select user_id , \n" +
            "  count(1) num, \n" +
            "  sum(bet) bet_amount, \n" +
            "  sum(validbet) validbet , \n" +
            "  sum(win_loss) win_loss  \n" +
            "  from game_record gr \n" +
            "  where bet_time >= {0} and bet_time <= {1}\n" +
            "  group by user_id \n" +
            " ) main_t on u.id = main_t.user_id\n" +
            " left join ( \n" +
            "  select user_id , sum(amount) wash_amount  \n" +
            "  from wash_code_change wcc  \n" +
            "  where platform = {3} and create_time >= {0} and create_time <= {1}\n" +
            "  group by user_id \n" +
            " ) wash_t on u.id = wash_t.user_id\n" +
            " left join ( \n" +
            "  select user_id , sum(ifnull(service_charge,0)) service_charge  \n" +
            "  from withdraw_order wo  \n" +
            "  where update_time >= {0} and update_time <= {1}\n" +
            "  group by user_id \n" +
            " ) withdraw_t on u.id = withdraw_t.user_id\n" +
            " left join ( \n" +
            "  select user_id , sum(amount) amount from share_profit_change spc  \n" +
            "  where create_time >= {0} and create_time <= {1}\n" +
            "  group by user_id  \n" +
            " ) pr on u.id=pr.user_id \n" +
            "   left join (\n" +
            "\tSELECT user_id, SUM(amount) as water FROM extract_points_change \n" +
            "\twhere create_time >= {0} and create_time <= {1}\n" +
            " and platform = {3} " +
            "\tgroup by user_id \n" +
            " ) ec on u.id = ec.user_id\n" +
            " where 1=1 and u.id = {2}";

    public static String sumSql = "select \n" +
            " sum(ifnull(main_t.num,0)) + sum(ifnull(goldenf_t.num,0)) num,\n" +
            " sum(ifnull(main_t.bet_amount,0)) + sum(ifnull(goldenf_t.bet_amount,0)) bet_amount ,\n" +
            " sum(ifnull(main_t.validbet,0)) + sum(ifnull(goldenf_t.bet_amount,0)) validbet ,\n" +
            " sum(ifnull(main_t.win_loss,0)) + sum(ifnull(goldenf_t.win_loss,0)) win_loss ,\n" +
            " sum(ifnull(wash_t.wash_amount,0)) wash_amount, \n" +
            " sum(ifnull(withdraw_t.service_charge,0)) service_charge, \n" +
            " sum(ifnull(pr.amount,0)) all_profit_amount, \n" +
            " sum(-(ifnull(main_t.win_loss,0)+ifnull(goldenf_t.win_loss,0)+ifnull(wash_t.wash_amount,0)+ifnull(ec.water,0))) avg_benefit,\n" +
            " sum(-(ifnull(main_t.win_loss,0)+ifnull(goldenf_t.win_loss,0)+ifnull(wash_t.wash_amount,0))-ifnull(pr.amount,0)-ifnull(ec.water,0)+ifnull(withdraw_t.service_charge,0)) total_amount,\n" +
            " sum(ifnull(ec.water, 0)) all_water\n" +
            "from user u\n" +
            "left join ( \n" +
            "  select user_id , \n" +
            "  count(1) num, \n" +
            "  sum(bet) bet_amount, \n" +
            "  sum(validbet) validbet , \n" +
            "  sum(win_loss) win_loss  \n" +
            "  from game_record gr \n" +
            "  where bet_time >= {0} and bet_time <= {1}\n" +
            "  group by user_id \n" +
            " ) main_t on u.id = main_t.user_id\n" +
            "  left join ( \n" +
            "  select user_id , \n" +
            "  count(1) num, \n" +
            "  sum(bet_amount) bet_amount, \n" +
            "  sum(win_amount-bet_amount) win_loss  \n" +
            "  from game_record_goldenf grg \n" +
            "  where create_at_str >= {0} and create_at_str <= {1}\n" +
            "  group by user_id \n" +
            " ) goldenf_t on u.id = goldenf_t.user_id\n" +
            " left join ( \n" +
            "  select user_id , sum(amount) wash_amount  \n" +
            "  from wash_code_change wcc  \n" +
            "  where create_time >= {0} and create_time <= {1}\n" +
            "  group by user_id \n" +
            " ) wash_t on u.id = wash_t.user_id\n" +
            " left join ( \n" +
            "  select user_id , sum(ifnull(service_charge,0)) service_charge  \n" +
            "  from withdraw_order wo  \n" +
            "  where update_time >= {0} and update_time <= {1}\n" +
            "  group by user_id \n" +
            " ) withdraw_t on u.id = withdraw_t.user_id\n" +
            " left join ( \n" +
            "  select user_id , sum(amount) amount from share_profit_change spc  \n" +
            "  where create_time >= {0} and create_time <= {1}\n" +
            "  group by user_id  \n" +
            " ) pr on u.id=pr.user_id \n" +
            "   left join (\n" +
            "\tSELECT user_id, SUM(amount) as water FROM extract_points_change \n" +
            "\twhere create_time >= {0} and create_time <= {1}\n" +
            "\tgroup by user_id \n" +
            " ) ec on u.id = ec.user_id\n";

    public static String WMSumSql = "select \n" +
            " sum(ifnull(main_t.num,0)) num,\n" +
            " sum(ifnull(main_t.bet_amount,0)) bet_amount ,\n" +
            " sum(ifnull(main_t.validbet,0)) validbet ,\n" +
            " sum(ifnull(main_t.win_loss,0)) win_loss ,\n" +
            " sum(ifnull(wash_t.wash_amount,0)) wash_amount, \n" +
            " sum(ifnull(withdraw_t.service_charge,0)) service_charge, \n" +
            " sum(ifnull(pr.amount,0)) all_profit_amount, \n" +
            " sum(-(ifnull(main_t.win_loss,0)+ifnull(wash_t.wash_amount,0)+ifnull(ec.water, 0))) avg_benefit, \n" +
            " sum(-(ifnull(main_t.win_loss,0)+ifnull(wash_t.wash_amount,0))-ifnull(pr.amount,0)-ifnull(ec.water,0) +ifnull(withdraw_t.service_charge,0)) total_amount,\n" +
            " ifnull(ec.water, 0) all_water\n" +
            "from user u\n" +
            "left join ( \n" +
            "  select user_id , \n" +
            "  count(1) num, \n" +
            "  sum(bet) bet_amount, \n" +
            "  sum(validbet) validbet , \n" +
            "  sum(win_loss) win_loss  \n" +
            "  from game_record gr \n" +
            "  where bet_time >= {0} and bet_time <= {1}\n" +
            "  group by user_id \n" +
            " ) main_t on u.id = main_t.user_id\n" +
            " left join ( \n" +
            "  select user_id , sum(amount) wash_amount  \n" +
            "  from wash_code_change wcc  \n" +
            "  where platform = {2} and create_time >= {0} and create_time <= {1}\n" +
            "  group by user_id \n" +
            " ) wash_t on u.id = wash_t.user_id\n" +
            " left join ( \n" +
            "  select user_id , sum(ifnull(service_charge,0)) service_charge  \n" +
            "  from withdraw_order wo  \n" +
            "  where update_time >= {0} and update_time <= {1}\n" +
            "  group by user_id \n" +
            " ) withdraw_t on u.id = withdraw_t.user_id\n" +
            " left join ( \n" +
            "  select user_id , sum(amount) amount from share_profit_change spc  \n" +
            "  where create_time >= {0} and create_time <= {1}\n" +
            "  group by user_id  \n" +
            " ) pr on u.id=pr.user_id \n" +
            "   left join (\n" +
            "\tSELECT user_id, SUM(amount) as water FROM extract_points_change \n" +
            "\twhere create_time >= {0} and create_time <= {1}\n" +
            " and platform = {2} "+
            "\tgroup by user_id \n" +
            " ) ec on u.id = ec.user_id";

    public static String PGAndCQ9SumSql = "select \n" +
            " sum(ifnull(goldenf_t.num,0)) num,\n" +
            " sum(ifnull(goldenf_t.bet_amount,0)) bet_amount ,\n" +
            " sum(ifnull(goldenf_t.bet_amount,0)) validbet ,\n" +
            " sum(ifnull(goldenf_t.win_loss,0)) win_loss ,\n" +
            " sum(ifnull(wash_t.wash_amount,0)) wash_amount, \n" +
            " sum(ifnull(withdraw_t.service_charge,0)) service_charge, \n" +
            " sum(ifnull(pr.amount,0)) all_profit_amount, \n" +
            " sum(-(ifnull(goldenf_t.win_loss,0)+ifnull(wash_t.wash_amount,0)+ifnull(ec.water, 0))) avg_benefit, \n" +
            " sum(-(ifnull(goldenf_t.win_loss,0)+ifnull(wash_t.wash_amount,0))-ifnull(pr.amount,0)-ifnull(ec.water,0)+ifnull(withdraw_t.service_charge,0)) total_amount,\n" +
            "sum(ifnull(ec.water, 0)) all_water\n" +
            "from user u\n" +
            " left join ( \n" +
            "  select user_id , \n" +
            "  count(1) num, \n" +
            "  sum(bet_amount) bet_amount, \n" +
            "  sum(win_amount-bet_amount) win_loss  \n" +
            "  from game_record_goldenf grg \n" +
            "  where vendor_code = {2} and create_at_str >= {0} and create_at_str <= {1}\n" +
            "  group by user_id \n" +
            " ) goldenf_t on u.id = goldenf_t.user_id\n" +
            " left join ( \n" +
            "  select user_id , sum(amount) wash_amount  \n" +
            "  from wash_code_change wcc  \n" +
            "  where platform = {2} and create_time >= {0} and create_time <= {1}\n" +
            "  group by user_id \n" +
            " ) wash_t on u.id = wash_t.user_id\n" +
            " left join ( \n" +
            "  select user_id , sum(ifnull(service_charge,0)) service_charge  \n" +
            "  from withdraw_order wo  \n" +
            "  where update_time >= {0} and update_time <= {1}\n" +
            "  group by user_id \n" +
            " ) withdraw_t on u.id = withdraw_t.user_id\n" +
            " left join ( \n" +
            "  select user_id , sum(amount) amount from share_profit_change spc  \n" +
            "  where create_time >= {0} and create_time <= {1}\n" +
            "  group by user_id  \n" +
            " ) pr on u.id=pr.user_id \n" +
            "   left join (\n" +
            "\tSELECT user_id, SUM(amount) as water FROM extract_points_change \n" +
            "\twhere create_time >= {0} and create_time <= {1}\n" +
            " and platform = {2} " +
            "\tgroup by user_id \n" +
            " ) ec on u.id = ec.user_id";
}
