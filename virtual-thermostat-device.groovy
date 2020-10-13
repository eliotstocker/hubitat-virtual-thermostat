metadata {
    definition (name: "Virtual Thermostat Device",
            namespace: "piratemedia",
            author: "Eliot S.") {
        capability "Thermostat"
        capability "Thermostat Heating Setpoint"
        capability "Thermostat Setpoint"
        capability "Sensor"
        capability "Actuator"

        command "refresh"

        command "setVirtualTemperature", ["number"]
        command "setHeatingStatus", ["string"]

        attribute "temperatureUnit", "string"
    }
}

def shouldReportInCentigrade() {
    try {
        def ts = getTemperatureScale();
        return ts == "C"
    } catch (e) {
        log.error e
    }
    return true;
}

def installed() {
    initialize()
}

def configure() {
    initialize()
}

private initialize() {
    setHeatingSetpoint(defaultTemp())
    setVirtualTemperature(defaultTemp())
    setHeatingStatus("off")
    setThermostatMode("off")
    sendEvent(name:"supportedThermostatModes",    value: ['heat', 'off'], displayed: false)
    sendEvent(name:"supportedThermostatFanModes", values: [], displayed: false)

    state.tempScale = "C"
}

def getTempColors() {
    def colorMap
    //getTemperatureScale() == "C"   wantMetric()
    if(shouldReportInCentigrade()) {
        colorMap = [
                // Celsius Color Range
                [value: 0, color: "#153591"],
                [value: 7, color: "#1e9cbb"],
                [value: 15, color: "#90d2a7"],
                [value: 23, color: "#44b621"],
                [value: 29, color: "#f1d801"],
                [value: 33, color: "#d04e00"],
                [value: 36, color: "#bc2323"]
        ]
    } else {
        colorMap = [
                // Fahrenheit Color Range
                [value: 40, color: "#153591"],
                [value: 44, color: "#1e9cbb"],
                [value: 59, color: "#90d2a7"],
                [value: 74, color: "#44b621"],
                [value: 84, color: "#f1d801"],
                [value: 92, color: "#d04e00"],
                [value: 96, color: "#bc2323"]
        ]
    }
}

def unitString() {  return shouldReportInCentigrade() ? "C": "F" }
def defaultTemp() { return shouldReportInCentigrade() ? 20 : 70 }
def lowRange() { return shouldReportInCentigrade() ? 9 : 45 }
def highRange() { return shouldReportInCentigrade() ? 45 : 113 }
def getRange() { return "${lowRange()}..${highRange()}" }

def getTemperature() {
    return device.currentValue("temperature")
}

def setHeatingSetpoint(temp) {
    def ctsp = device.currentValue("thermostatSetpoint");
    def chsp = device.currentValue("heatingSetpoint");

    if(ctsp != temp || chsp != temp) {
        sendEvent(name:"thermostatSetpoint", value: temp, unit: unitString(), displayed: false)
        sendEvent(name:"heatingSetpoint", value: temp, unit: unitString())
    }
}

def parse(data) {
    log.debug "parse data: $data"
}

def refresh() {
    log.trace "Executing refresh"
    sendEvent(name: "supportedThermostatModes",    value: ['heat', 'off'], displayed: false)
    sendEvent(name: "supportedThermostatFanModes", values: [], displayed: false)
}

def getThermostatMode() {
    return device.currentValue("thermostatMode")
}

def getOperatingState() {
    return device.currentValue("thermostatOperatingState")
}

def getThermostatSetpoint() {
    return device.currentValue("thermostatSetpoint")
}

def getHeatingSetpoint() {
    return device.currentValue("heatingSetpoint")
}

def setThermostatMode(mode) {
    if(device.currentValue("thermostatMode") != mode) {
        sendEvent(name: "thermostatMode", value: mode)
    }
}

def changeMode() {
    def val = device.currentValue("thermostatMode") == "off" ? "heat" : "off"
    setThermostatMode(val)
    return val
}

def setVirtualTemperature(temp) {
    sendEvent(name:"temperature", value: temp, unit: unitString(), displayed: true)
}

def setHeatingStatus(string) {
    if(device.currentValue("thermostatOperatingState") != string) {
        sendEvent(name:"thermostatOperatingState", value: string)
    }
}