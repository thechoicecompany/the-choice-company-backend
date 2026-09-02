package com.thechoicecompany.enums;

public enum InventoryAction {
    RESTOCK,      // Added units to warehouse
    ADJUSTMENT,   // Manual correction (count discrepancy)
    RESERVED,     // Units locked for a confirmed order
    RELEASED,     // Reserved units freed (order cancelled)
    DISPATCHED,   // Units shipped out — reduce stock
    DAMAGED,      // Units written off as damaged/lost
    RETURNED      // Customer returned units back to stock
}
