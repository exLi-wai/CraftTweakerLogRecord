import mods.crt_log_record.CrtLog;

//ERROR
val shouldLog = true;
mods.crt_log_record.CrtLog.error("basic error message");
mods.crt_log_record.CrtLog.error(shouldLog, "conditional error (logged)");
mods.crt_log_record.CrtLog.error(false, "conditional error (skipped)");
mods.crt_log_record.CrtLog.error("formatted error: %s | %s", ["hello", "world"]);
mods.crt_log_record.CrtLog.error(1001, "error with code");
mods.crt_log_record.CrtLog.error("repeated error", 3);
mods.crt_log_record.CrtLog.error(3.14159, "pi value error");
mods.crt_log_record.CrtLog.error(1712345678901, "timestamp error");

//FATAL
mods.crt_log_record.CrtLog.fatal("basic fatal message");
mods.crt_log_record.CrtLog.fatal(shouldLog, "conditional fatal (logged)");
mods.crt_log_record.CrtLog.fatal(false, "conditional fatal (skipped)");
mods.crt_log_record.CrtLog.fatal("formatted fatal: %s | %s", ["foo", "bar"]);
mods.crt_log_record.CrtLog.fatal(2001, "fatal with code");
mods.crt_log_record.CrtLog.fatal("repeated fatal", 2);
mods.crt_log_record.CrtLog.fatal(2.71828, "e value fatal");
mods.crt_log_record.CrtLog.fatal(1712345678902, "timestamp fatal");

//WARN
mods.crt_log_record.CrtLog.warn("basic warn message");
mods.crt_log_record.CrtLog.warn(shouldLog, "conditional warn (logged)");
mods.crt_log_record.CrtLog.warn(false, "conditional warn (skipped)");
mods.crt_log_record.CrtLog.warn(3001, "warn with code");
