package codes.chrishorner.socketweather.data

import com.squareup.moshi.Types
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type

/**
 * The BOM API returns the data we actually care about wrapped up in an [Envelope] object.
 *
 * This [Converter.Factory] automatically unwraps these responses, allowing our Retrofit
 * interface to declare the return types without the layer of wrapping.
 */
object EnvelopeConverter : Converter.Factory() {

  override fun responseBodyConverter(
      type: Type,
      annotations: Array<Annotation>,
      retrofit: Retrofit
  ): Converter<ResponseBody, *>? {

    val envelopedType = Types.newParameterizedType(Envelope::class.java, type)
    val delegate: Converter<ResponseBody, Envelope<Any>>? =
        retrofit.nextResponseBodyConverter(this, envelopedType, annotations)

    return Unwrapper(delegate)
  }

  private class Unwrapper<T>(
      private val delegate: Converter<ResponseBody, Envelope<T>>?
  ) : Converter<ResponseBody, T> {

    override fun convert(value: ResponseBody): T? {
      return delegate?.convert(value)?.data
    }
  }
}
