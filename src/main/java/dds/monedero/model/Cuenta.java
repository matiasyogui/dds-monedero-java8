package dds.monedero.model;

import dds.monedero.exceptions.MaximaCantidadDepositosException;
import dds.monedero.exceptions.MaximoExtraccionDiarioException;
import dds.monedero.exceptions.MontoNegativoException;
import dds.monedero.exceptions.SaldoMenorException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Cuenta {

  private double saldo = 0;
  private List<Movimiento> movimientos = new ArrayList<>();

  public void setMovimientos(List<Movimiento> movimientos) {
    this.movimientos = movimientos;
  }

  public void poner(double cuanto) {
    validarMontoMenorOIgualACero(cuanto);
    if (getMovimientos().stream().filter(movimiento -> movimiento.fueDepositado(LocalDate.now())).count() >= 3) {
      throw new MaximaCantidadDepositosException("Ya excedio los " + 3 + " depositos diarios");
    }

    agregarACuenta(new Movimiento(LocalDate.now(), cuanto, true));
  }

  public void sacar(double cuanto) {
    validarMontoMenorOIgualACero(cuanto);
    if (getSaldo() - cuanto < 0) {
      throw new SaldoMenorException("No puede sacar mas de " + getSaldo() + " $");
    }
    double montoExtraidoHoy = getMontoExtraidoA(LocalDate.now());
    double limite = 1000 - montoExtraidoHoy;
    if (cuanto > limite) {
      throw new MaximoExtraccionDiarioException("No puede extraer mas de $ " + 1000
          + " diarios, límite: " + limite);
    }
    agregarACuenta(new Movimiento(LocalDate.now(), cuanto, false));
  }

  public void validarMontoMenorOIgualACero(double monto){
    if(monto <= 0) {
      throw new MontoNegativoException(monto + ": el monto a ingresar debe ser un valor positivo.");
    }
  }

  public void agregarMovimiento(Movimiento movimiento) {
    movimientos.add(movimiento);
  }

  public double getMontoExtraidoA(LocalDate fecha) {
    return sumarMontoDeMovimientos(filtrarMovimientosPorFueExtraido(fecha));
  }

  public List<Movimiento> filtrarMovimientosPorFueExtraido(LocalDate fecha) {
    return getMovimientos().stream().filter(movimiento -> movimiento.fueExtraido(fecha)).collect(Collectors.toList());
  }

  public Double sumarMontoDeMovimientos(List<Movimiento> listaDeMovimientos) {
    return listaDeMovimientos.stream().mapToDouble(Movimiento::getMonto).sum();
  }

  public List<Movimiento> getMovimientos() {
    return movimientos;
  }

  public double getSaldo() {
    return saldo;
  }

  public void setSaldo(double saldo) {
    this.saldo = saldo;
  }

  public void agregarACuenta(Movimiento movimiento) {
    setSaldo(calcularValor(movimiento));
    agregarMovimiento(movimiento);
  }

  public double calcularValor(Movimiento movimiento) {
    return getSaldo() + movimiento.getMontoPorDeposito();
  }
}
