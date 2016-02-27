package org.mdedetrich.stripe.v1

import enumeratum._
import org.joda.time.DateTime
import org.mdedetrich.stripe.v1.Transfers._
import play.api.libs.json._
import play.api.libs.functional.syntax._

object Balances {

  sealed abstract class FeeType(val id: String) extends EnumEntry {
    override val entryName = id
  }

  object FeeType extends Enum[FeeType] {

    val values = findValues

    case object ApplicationFee extends FeeType("application_fee")

    case object StripeFee extends FeeType("stripe_fee")

    case object Tax extends FeeType("tax")

  }

  implicit val feeTypeFormats = EnumFormats.formats(FeeType, insensitive = true)

  case class FeeDetails(amount: BigDecimal,
                        application: String,
                        currency: Currency,
                        description: String,
                        `type`: FeeType)

  implicit val feeDetailReads: Reads[FeeDetails] = (
    (__ \ "amount").read[BigDecimal] ~
      (__ \ "application").read[String] ~
      (__ \ "currency").read[Currency] ~
      (__ \ "description").read[String] ~
      (__ \ "type").read[FeeType]
    ) ((amount, application, currency, description, `type`) =>
    FeeDetails(amount, application, currency, description, `type`)
  )

  implicit val feeDetailWrites: Writes[FeeDetails] =
    Writes((feeDetails: FeeDetails) =>
      Json.obj(
        "amount" -> feeDetails.amount,
        "application" -> feeDetails.application,
        "currency" -> feeDetails.currency,
        "description" -> feeDetails.description,
        "type" -> feeDetails.`type`
      )
    )

  case class SourcedTransfers(data: List[Transfers.Transfer],
                              hasMore: Boolean,
                              totalCount: Long,
                              url: String)

  implicit val sourcedTransfersReads: Reads[SourcedTransfers] = (
    (__ \ "data").read[List[Transfer]] ~
      (__ \ "has_more").read[Boolean] ~
      (__ \ "total_count").read[Long] ~
      (__ \ "url").read[String]
    ) ((data, hasMore, totalCount, url) =>
    SourcedTransfers(data, hasMore, totalCount, url)
  )

  implicit val sourcedTransfersWrites: Writes[SourcedTransfers] =
    Writes((sourcedTransfers: SourcedTransfers) =>
      Json.obj(
        "data" -> sourcedTransfers.data,
        "has_more" -> sourcedTransfers.hasMore,
        "total_count" -> sourcedTransfers.totalCount,
        "url" -> sourcedTransfers.url
      )
    )

  sealed abstract class Type(val id: String) extends EnumEntry {
    override val entryName = id
  }

  object Type extends Enum[Type] {

    val values = findValues

    case object Charge extends Type("charge")

    case object Refund extends Type("refund")

    case object Adjustment extends Type("adjustment")

    case object ApplicationFee extends Type("application_fee")

    case object ApplicationFeeRefund extends Type("application_fee_refund")

    case object Transfer extends Type("transfer")

    case object TransferCancel extends Type("transfer_cancel")

    case object TransferRefund extends Type("transfer_refund")

    case object TransferFailure extends Type("transfer_failure")

  }

  implicit val typeFormats = EnumFormats.formats(Type, insensitive = true)

  sealed abstract class Status(val id: String) extends EnumEntry {
    override val entryName = id
  }

  object Status extends Enum[Status] {

    val values = findValues

    case object Available extends Status("available")

    case object Pending extends Status("pending")

  }

  implicit val statusFormats = EnumFormats.formats(Status, insensitive = true)

  case class BalanceTransaction(id: String,
                                amount: BigDecimal,
                                availableOn: DateTime,
                                created: DateTime,
                                currency: Currency,
                                description: String,
                                fee: BigDecimal,
                                feeDetails: List[FeeDetails],
                                net: BigDecimal,
                                source: String,
                                sourcedTransfers: SourcedTransfers,
                                status: Status,
                                `type`: Type) extends StripeObject

  implicit val balanceTransactionReads: Reads[BalanceTransaction] = (
    (__ \ "id").read[String] ~
      (__ \ "amount").read[BigDecimal] ~
      (__ \ "available_on").read[Long].map { timestamp => new DateTime(timestamp * 1000) } ~
      (__ \ "created").read[Long].map { timestamp => new DateTime(timestamp * 1000) } ~
      (__ \ "currency").read[Currency] ~
      (__ \ "description").read[String] ~
      (__ \ "fee").read[BigDecimal] ~
      (__ \ "fee_details").read[List[FeeDetails]] ~
      (__ \ "net").read[BigDecimal] ~
      (__ \ "source").read[String] ~
      (__ \ "sourced_transfers").read[SourcedTransfers] ~
      (__ \ "status").read[Status] ~
      (__ \ "type").read[Type]
    ).tupled.map(BalanceTransaction.tupled)

  implicit val balanceTransactionWrites: Writes[BalanceTransaction] =
    Writes((balanceTransaction: BalanceTransaction) =>
      Json.obj(
        "id" -> balanceTransaction.id,
        "object" -> "balance_transaction",
        "amount" -> balanceTransaction.amount,
        "currency" -> balanceTransaction.currency,
        "available_on" -> balanceTransaction.availableOn.getMillis / 1000,
        "created" -> balanceTransaction.created.getMillis / 1000,
        "description" -> balanceTransaction.description,
        "fee" -> balanceTransaction.fee,
        "fee_details" -> balanceTransaction.feeDetails,
        "net" -> balanceTransaction.net,
        "source" -> balanceTransaction.source,
        "sourced_transfers" -> balanceTransaction.sourcedTransfers,
        "status" -> balanceTransaction.status,
        "type" -> balanceTransaction.`type`
      )
    )
}
