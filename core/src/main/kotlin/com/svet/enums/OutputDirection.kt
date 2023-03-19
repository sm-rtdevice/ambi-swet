package com.svet.enums

/**
 * Направление вывода цветов на LED ленту.
 **/
enum class OutputDirection {
    CLOCKWISE,       // <---- по часовой стреке
    COUNTERCLOCKWISE // ----> против часовой стрелки

    /**
     *    CLOCKWISE:
     * --------------->
     * |     SVET     |
     * <----  ||  <----
     *      ======
     *
     * COUNTERCLOCKWISE:
     * <---------------
     * |     SVET     |
     * ---->  ||  ---->
     *      ======
     */

}
