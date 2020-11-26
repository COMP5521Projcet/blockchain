package com.fan.blockchain.transaction;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpendableOutputResult {
    // 交易时的支付金额
    private int accumulated;
    // 能够支付上述金额的未花费的交易(交易ID,交易输出的index)
    private Map<String,int[]> unspentOuts;
}
