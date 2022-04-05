/*
   Управление лентой на WS2812
*/

//----------------------НАСТРОЙКИ-----------------------
#define NUM_LEDS 94          // число светодиодов в ленте
#define DI_PIN 13            // пин, к которому подключена лента
#define start_flashes 0      // проверка цветов при запуске (1 - включить, 0 - выключить)
//----------------------НАСТРОЙКИ-----------------------

#define serialRate 115200    // скорость связи с ПК

#include <FastLED.h>
CRGB leds[NUM_LEDS];  // создаём ленту
uint8_t prefix[] = {'A', 'd', 'a'}, hi, lo, chk, i;  // кодовое слово Ada для связи

void setup()
{
  FastLED.addLeds<WS2812, DI_PIN, GRB>(leds, NUM_LEDS);  // инициализация светодиодов

  // вспышки красным синим и зелёным при запуске
  if (start_flashes) {
    LEDS.showColor(CRGB(255, 0, 0));
    delay(500);
    LEDS.showColor(CRGB(0, 255, 0));
    delay(500);
    LEDS.showColor(CRGB(0, 0, 255));
    delay(500);
    LEDS.showColor(CRGB(0, 0, 0));
  }

  Serial.begin(serialRate);
  Serial.print("Ada\n");     // Связаться с компом
}

void loop() {

  for (i = 0; i < sizeof prefix; ++i) {
    waitLoop: while (!Serial.available()) ;;
    if (prefix[i] == Serial.read()) continue;
    i = 0;
    goto waitLoop;
  }

  while (!Serial.available()) ;;
  hi = Serial.read();
  while (!Serial.available()) ;;
  lo = Serial.read();
  while (!Serial.available()) ;;
  chk = Serial.read();

  if (chk != (hi ^ lo ^ 0x55)){
    i = 0;
    goto waitLoop;
  }

  memset(leds, 0, NUM_LEDS * sizeof(struct CRGB));
  for (uint8_t i = 0; i < NUM_LEDS; i++) {
    byte r, g, b;
    // читаем данные для каждого цвета
    while (!Serial.available());
    r = Serial.read();
    while (!Serial.available());
    g = Serial.read();
    while (!Serial.available());
    b = Serial.read();
    leds[i].r = r;
    leds[i].g = g;
    leds[i].b = b;
  }
  FastLED.show();  // записываем цвета в ленту
}
