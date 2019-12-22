package codes.chrishorner.socketweather.data

import com.squareup.moshi.Types
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type

object EnvelopeConverter : Converter.Factory() {

  override fun responseBodyConverter(
      type: Type,
      annotations: Array<Annotation>,
      retrofit: Retrofit
  ): Converter<ResponseBody, *>? {

    val envelopedType = Types.newParameterizedType(Envelope::class.java, type).rawType
    val delegate: Converter<ResponseBody, Envelope<*>> =
        retrofit.nextResponseBodyConverter(this, envelopedType, annotations)

    return Converter<ResponseBody, Any?> { body -> delegate.convert(body)?.data }
  }
}
