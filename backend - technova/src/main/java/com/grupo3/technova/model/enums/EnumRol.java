package com.grupo3.technova.model.enums;

// Enum que representa los roles posibles de un usuario.
// Al usar un enum en vez de un String libre, el compilador nos avisa
// si escribimos mal un rol — "ADMINSTRADOR" con un enum no compila,
// con un String nadie se entera hasta que falla en ejecución.
public enum EnumRol {
    CLIENTE,
    OFICINA,
    ADMINISTRADOR
}